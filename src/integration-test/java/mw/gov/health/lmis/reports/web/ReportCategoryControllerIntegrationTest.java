package mw.gov.health.lmis.reports.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import mw.gov.health.lmis.reports.domain.ReportCategory;
import mw.gov.health.lmis.reports.dto.ReportCategoryDto;
import mw.gov.health.lmis.reports.repository.ReportCategoryRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class ReportCategoryControllerIntegrationTest
    extends BaseWebIntegrationTest {
  private static final String RESOURCE_URL = "/api/reports/reportCategories";
  private static final String ID_URL = RESOURCE_URL + "/{id}";

  @MockBean
  private ReportCategoryRepository reportCategoryRepository;

  private UUID reportCategoryId;
  private ReportCategory reportCategory;

  @Before
  public void setUp() {
    reportCategory = new ReportCategory();
    reportCategoryId = UUID.randomUUID();
    reportCategory.setId(reportCategoryId);
    reportCategory.setName("Test Category");
  }

  @Test
  public void shouldReturnReportCategoryWhenCategoryExists() {
    when(reportCategoryRepository.findOne(reportCategoryId)).thenReturn(reportCategory);

    // When & Then
    ReportCategoryDto response = restAssured.given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(ID_URL, reportCategoryId)
        .then()
        .statusCode(HttpStatus.OK.value())
        .extract().as(ReportCategoryDto.class);

    // Verify
    assertNotNull(response);
    assertEquals(response.getId(), reportCategory.getId());
    assertEquals(response.getName(), reportCategory.getName());

    verify(reportCategoryRepository).findOne(reportCategoryId);
  }

  @Test
  public void shouldUpdateReportCategoryWhenValidDataProvided() {
    // Given
    ReportCategoryDto updatedDto = new ReportCategoryDto();
    updatedDto.setId(reportCategoryId);
    updatedDto.setName("Updated Category Name");

    ReportCategory updatedCategory = new ReportCategory();
    updatedCategory.setId(reportCategoryId);
    updatedCategory.setName(updatedDto.getName());

    when(reportCategoryRepository.existsByIdIsNotAndName(
        reportCategoryId, updatedDto.getName())).thenReturn(false);
    when(reportCategoryRepository.findOne(reportCategoryId)).thenReturn(reportCategory);
    when(reportCategoryRepository.save(any(ReportCategory.class))).thenReturn(updatedCategory);

    // When & Then
    ReportCategoryDto response = restAssured.given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam(ACCESS_TOKEN, getToken())
        .body(updatedDto)
        .when()
        .put(ID_URL, reportCategoryId)
        .then()
        .statusCode(HttpStatus.OK.value())
        .extract().as(ReportCategoryDto.class);

    // Verify
    assertNotNull(response);
    assertEquals(response.getId(), updatedDto.getId());
    assertEquals(response.getName(), updatedDto.getName());

    verify(reportCategoryRepository).existsByIdIsNotAndName(
        reportCategoryId, updatedDto.getName());
    verify(reportCategoryRepository).findOne(reportCategoryId);
    verify(reportCategoryRepository).save(any(ReportCategory.class));
  }

}
