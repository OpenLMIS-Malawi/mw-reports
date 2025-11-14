package mw.gov.health.lmis.reports.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import mw.gov.health.lmis.reports.domain.DashboardReport;
import mw.gov.health.lmis.reports.domain.ReportCategory;
import mw.gov.health.lmis.reports.dto.DashboardReportDto;
import mw.gov.health.lmis.reports.dto.external.RightDto;
import mw.gov.health.lmis.reports.dto.external.RightType;
import mw.gov.health.lmis.reports.repository.DashboardReportRepository;
import mw.gov.health.lmis.reports.repository.ReportCategoryRepository;
import mw.gov.health.lmis.reports.service.referencedata.RightReferenceDataService;
import mw.gov.health.lmis.utils.ReportType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class DashboardReportControllerIntegrationTest
    extends BaseWebIntegrationTest {
  private static final String RESOURCE_URL = "/api/reports/dashboardReports";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String RIGHT = "_RIGHT";

  @MockBean
  private DashboardReportRepository dashboardReportRepository;

  @MockBean
  private ReportCategoryRepository reportCategoryRepository;

  @MockBean
  private RightReferenceDataService rightReferenceDataService;

  private UUID reportId;
  private DashboardReport dashboardReport;
  private ReportCategory reportCategory;
  private RightDto requiredRight;

  @Before
  public void setUp() {
    reportCategory = new ReportCategory();
    reportCategory.setId(UUID.randomUUID());
    reportCategory.setName("Sample Category");

    dashboardReport = new DashboardReport();
    reportId = UUID.randomUUID();
    dashboardReport.setId(reportId);
    dashboardReport.setName("Sample Report");
    dashboardReport.setUrl("http://example.com");
    dashboardReport.setType(ReportType.POWERBI);
    dashboardReport.setEnabled(true);
    dashboardReport.setShowOnHomePage(true);
    dashboardReport.setCategory(reportCategory);
    dashboardReport.setRightName((dashboardReport.getName() + RIGHT).toUpperCase());

    requiredRight = new RightDto();
    requiredRight.setId(UUID.randomUUID());
    requiredRight.setName(dashboardReport.getRightName());
    requiredRight.setType(RightType.REPORTS);
  }

  @Test
  public void shouldReturnDashboardReportWhenReportExists() {
    // Given
    when(dashboardReportRepository.findOne(reportId)).thenReturn(dashboardReport);

    // When & Then
    DashboardReportDto response = restAssured.given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(ID_URL, reportId)
        .then()
        .statusCode(HttpStatus.OK.value())
        .extract().as(DashboardReportDto.class);

    // Verify
    assertNotNull(response);
    assertNotNull(response.getCategory());
    assertEquals(response.getId(), dashboardReport.getId());
    assertEquals(response.getUrl(), dashboardReport.getUrl());
    assertEquals(response.getType(), dashboardReport.getType());
    assertEquals(response.isEnabled(), dashboardReport.isEnabled());
    assertEquals(response.isShowOnHomePage(), dashboardReport.isShowOnHomePage());
    assertEquals(response.getCategory().getId(), reportCategory.getId());
    assertEquals(response.getCategory().getName(), reportCategory.getName());

    verify(dashboardReportRepository).findOne(reportId);
  }

  @Test
  public void shouldReturn404WhenReportDoesNotExist() {
    // Given
    when(dashboardReportRepository.findOne(reportId)).thenReturn(null);

    // When & Then
    restAssured.given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(ID_URL, reportId)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  public void shouldReturn401WhenUnauthorized() {
    // When & Then
    restAssured.given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(ID_URL, reportId)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  public void shouldDeleteDashboardReport() {
    // Given
    when(dashboardReportRepository.findOne(reportId)).thenReturn(dashboardReport);
    when(rightReferenceDataService.findRight(dashboardReport.getRightName()))
        .thenReturn(requiredRight);
    doNothing().when(dashboardReportRepository).delete(dashboardReport);
    doNothing().when(rightReferenceDataService).delete(requiredRight.getId());

    // When & Then
    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .delete(ID_URL, reportId)
        .then()
        .statusCode(HttpStatus.NO_CONTENT.value());

    // Verify
    verify(dashboardReportRepository).findOne(reportId);
    verify(dashboardReportRepository).delete(dashboardReport);
    verify(rightReferenceDataService).findRight(requiredRight.getName());
    verify(rightReferenceDataService).delete(requiredRight.getId());
  }

  @Test
  public void shouldThrowExceptionWhenDeletingNonExistentDashboardReport() {
    // Given
    UUID reportId = UUID.randomUUID();
    when(dashboardReportRepository.findOne(reportId)).thenReturn(null);

    // When & Then
    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .delete(ID_URL, reportId)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());

    // Verify
    verify(dashboardReportRepository).findOne(reportId);
  }

  @Test
  public void shouldCreateDashboardReport() throws Exception {
    // Given
    when(reportCategoryRepository.findOne(any(UUID.class))).thenReturn(reportCategory);
    doNothing().when(rightReferenceDataService).save(any(RightDto.class));

    DashboardReportDto dto = new DashboardReportDto();
    dto.setName("Unique Report Name");
    dto.setUrl("http://url.com");
    dto.setType(ReportType.POWERBI);
    dto.setEnabled(true);
    dto.setShowOnHomePage(true);
    dto.setCategory(reportCategory);
    dto.setRightName((dto.getName() + RIGHT).toUpperCase());

    DashboardReport createdReport = new DashboardReport();
    createdReport.updateFrom(dto);

    when(dashboardReportRepository.existsByName(dto.getName())).thenReturn(false);
    when(dashboardReportRepository.save(any(DashboardReport.class))).thenReturn(createdReport);

    // When & Then
    DashboardReportDto response = restAssured.given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam(ACCESS_TOKEN, getToken())
        .body(dto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.OK.value())
        .extract().as(DashboardReportDto.class);

    // Verify
    assertNotNull(response);
    assertNotNull(response.getCategory());
    assertEquals(dto.getName(), response.getName());
    assertEquals(dto.getUrl(), response.getUrl());
    assertEquals(dto.getType(), response.getType());
    assertEquals(dto.isShowOnHomePage(), response.isShowOnHomePage());
    assertEquals(dto.isEnabled(), response.isEnabled());
    assertEquals(dto.getCategory().getId(), response.getCategory().getId());
    assertEquals(dto.getCategory().getName(), response.getCategory().getName());

    verify(reportCategoryRepository).findOne(reportCategory.getId());
    verify(dashboardReportRepository).existsByName(dto.getName());
    verify(dashboardReportRepository).save(any(DashboardReport.class));
    verify(rightReferenceDataService).save(any(RightDto.class));
  }

  @Test
  public void shouldThrowExceptionWhenCreatingDashboardReportWithDuplicateName() {
    // Given
    DashboardReportDto dto = new DashboardReportDto();
    dto.setName("Duplicate Report Name");
    dto.setUrl("http://url.com");
    dto.setType(ReportType.POWERBI);
    dto.setEnabled(true);
    dto.setShowOnHomePage(true);
    dto.setCategory(reportCategory);
    dto.setRightName((dto.getName() + RIGHT).toUpperCase());

    when(dashboardReportRepository.existsByName(dto.getName())).thenReturn(true);

    // When & Then
    restAssured.given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam(ACCESS_TOKEN, getToken())
        .body(dto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());

    // Verify
    verify(dashboardReportRepository).existsByName(dto.getName());
    verify(dashboardReportRepository, never()).save(any(DashboardReport.class));
  }

  @Test
  public void shouldUpdateDashboardReport() {
    // Given
    DashboardReportDto updatedDto = new DashboardReportDto();
    updatedDto.setId(reportId);
    updatedDto.setName("Updated Report Name");
    updatedDto.setUrl("http://new-url.com");
    updatedDto.setEnabled(false);
    updatedDto.setShowOnHomePage(true);
    updatedDto.setCategory(reportCategory);

    when(dashboardReportRepository.findOne(reportId)).thenReturn(dashboardReport);
    when(reportCategoryRepository.findOne(any(UUID.class))).thenReturn(reportCategory);

    // When & Then
    DashboardReportDto response = restAssured.given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam(ACCESS_TOKEN, getToken())
        .body(updatedDto)
        .when()
        .put(ID_URL, reportId)
        .then()
        .statusCode(HttpStatus.OK.value())
        .extract().as(DashboardReportDto.class);

    // Verify
    assertNotNull(response);
    assertNotNull(response.getCategory());
    assertEquals(updatedDto.getName(), response.getName());
    assertEquals(updatedDto.getUrl(), response.getUrl());
    assertEquals(updatedDto.isEnabled(), response.isEnabled());
    assertEquals(updatedDto.isShowOnHomePage(), response.isShowOnHomePage());
    assertEquals(updatedDto.getCategory().getId(), response.getCategory().getId());
    assertEquals(updatedDto.getCategory().getName(), response.getCategory().getName());

    verify(dashboardReportRepository).findOne(reportId);
    verify(dashboardReportRepository).save(any(DashboardReport.class));
  }

  @Test
  public void shouldThrowExceptionWhenReportIdMismatchDuringUpdate() {
    // Given
    UUID mismatchedId = UUID.randomUUID();
    DashboardReportDto updatedDto = new DashboardReportDto();
    updatedDto.setId(mismatchedId);
    updatedDto.setName("Updated Report Name");
    updatedDto.setUrl("http://new-url.com");
    updatedDto.setEnabled(false);
    updatedDto.setShowOnHomePage(true);
    updatedDto.setCategory(reportCategory);

    // When & Then
    restAssured.given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam(ACCESS_TOKEN, getToken())
        .body(updatedDto)
        .when()
        .put(ID_URL, reportId)
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());

    // Verify
    verifyZeroInteractions(dashboardReportRepository);
  }

  @Test
  public void shouldThrowNotFoundExceptionWhenReportDoesNotExist() {
    // Given
    UUID reportId = UUID.randomUUID();
    dashboardReport.setId(reportId);

    when(dashboardReportRepository.findOne(reportId)).thenReturn(null);

    // When & Then
    restAssured.given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam(ACCESS_TOKEN, getToken())
        .body(dashboardReport)
        .when()
        .put(ID_URL, reportId)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());

    // Verify
    verify(dashboardReportRepository).findOne(reportId);
    verifyNoMoreInteractions(dashboardReportRepository);
  }

}
