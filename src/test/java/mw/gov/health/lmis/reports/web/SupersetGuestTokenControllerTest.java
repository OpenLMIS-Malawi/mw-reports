package mw.gov.health.lmis.reports.web;

import static mw.gov.health.lmis.reports.i18n.PermissionMessageKeys.ERROR_NO_PERMISSION;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import mw.gov.health.lmis.reports.exception.NotFoundMessageException;
import mw.gov.health.lmis.reports.exception.PermissionMessageException;
import mw.gov.health.lmis.reports.repository.DashboardReportRepository;
import mw.gov.health.lmis.reports.service.SupersetService;
import mw.gov.health.lmis.reports.service.ViewPermissionService;
import mw.gov.health.lmis.utils.Message;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class SupersetGuestTokenControllerTest {

  private static final String EMBEDDED_UUID = "test-dashboard-uuid";
  private static final String USERNAME = "testuser";
  private static final String GUEST_TOKEN = "mock-guest-token";

  @Mock
  private SupersetService supersetService;

  @Mock
  private DashboardReportRepository dashboardReportRepository;

  @Mock
  private ViewPermissionService viewPermissionService;

  @InjectMocks
  private SupersetGuestTokenController controller;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(USERNAME, "password")
    );
  }

  @After
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void shouldReturnGuestToken() {
    when(dashboardReportRepository.existsByEmbeddedUuid(EMBEDDED_UUID)).thenReturn(true);
    when(supersetService.getGuestToken(EMBEDDED_UUID, USERNAME, USERNAME, USERNAME))
        .thenReturn(GUEST_TOKEN);

    Map<String, String> result = controller.getGuestToken(EMBEDDED_UUID);

    assertEquals(GUEST_TOKEN, result.get("token"));
    assertEquals(1, result.size());
    verify(viewPermissionService).canViewReports(null);
  }

  @Test(expected = NotFoundMessageException.class)
  public void shouldThrowNotFoundWhenDashboardDoesNotExist() {
    when(dashboardReportRepository.existsByEmbeddedUuid(EMBEDDED_UUID)).thenReturn(false);

    try {
      controller.getGuestToken(EMBEDDED_UUID);
    } finally {
      verify(supersetService, never()).getGuestToken(
          EMBEDDED_UUID, USERNAME, USERNAME, USERNAME);
    }
  }

  @Test(expected = PermissionMessageException.class)
  public void shouldThrowWhenUserLacksReportsViewRight() {
    doThrow(new PermissionMessageException(new Message(ERROR_NO_PERMISSION)))
        .when(viewPermissionService).canViewReports(null);

    try {
      controller.getGuestToken(EMBEDDED_UUID);
    } finally {
      verify(dashboardReportRepository, never()).existsByEmbeddedUuid(EMBEDDED_UUID);
      verify(supersetService, never()).getGuestToken(
          EMBEDDED_UUID, USERNAME, USERNAME, USERNAME);
    }
  }
}
