package mw.gov.health.lmis.reports.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import mw.gov.health.lmis.reports.dto.DashboardReportDto;
import mw.gov.health.lmis.reports.exception.ValidationMessageException;
import mw.gov.health.lmis.reports.i18n.DashboardReportMessageKeys;
import mw.gov.health.lmis.reports.repository.DashboardReportRepository;
import mw.gov.health.lmis.reports.repository.ReportCategoryRepository;
import mw.gov.health.lmis.reports.service.referencedata.RightReferenceDataService;
import mw.gov.health.lmis.utils.Message;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.UnusedPrivateField")
public class DashboardReportServiceTest {

  @Mock
  private ReportCategoryRepository reportCategoryRepository;

  @Mock
  private DashboardReportRepository dashboardReportRepository;

  @Mock
  private RightReferenceDataService rightReferenceDataService;

  @Mock
  private PermissionService permissionService;

  @Mock
  private ViewPermissionService viewPermissionService;

  @InjectMocks
  private DashboardReportService dashboardReportService;

  @Test
  public void shouldRejectCreateWhenBothUrlAndEmbeddedUuidAreBlank() {
    DashboardReportDto dto = new DashboardReportDto();
    dto.setName("Report");
    dto.setUrl(null);
    dto.setEmbeddedUuid("");

    try {
      dashboardReportService.createDashboardReport(dto);
      fail("Expected ValidationMessageException");
    } catch (ValidationMessageException ex) {
      assertEquals(new Message(DashboardReportMessageKeys.ERROR_URL_OR_EMBEDDED_UUID_REQUIRED),
          ex.asMessage());
    }
  }

  @Test
  public void shouldRejectUpdateWhenBothUrlAndEmbeddedUuidAreBlank() {
    DashboardReportDto dto = new DashboardReportDto();
    dto.setName("Report");
    dto.setUrl("");
    dto.setEmbeddedUuid(null);

    try {
      dashboardReportService.updateDashboardReport(UUID.randomUUID(), dto);
      fail("Expected ValidationMessageException");
    } catch (ValidationMessageException ex) {
      assertEquals(new Message(DashboardReportMessageKeys.ERROR_URL_OR_EMBEDDED_UUID_REQUIRED),
          ex.asMessage());
    }
  }
}
