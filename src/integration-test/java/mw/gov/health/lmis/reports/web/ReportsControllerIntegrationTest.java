package mw.gov.health.lmis.reports.web;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsArrayContainingInAnyOrder.arrayContainingInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;

import guru.nidi.ramltester.junit.RamlMatchers;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import mw.gov.health.lmis.reports.dto.external.GeographicLevelDto;
import mw.gov.health.lmis.reports.dto.external.GeographicZoneDto;
import mw.gov.health.lmis.reports.dto.external.OrderableDto;
import mw.gov.health.lmis.reports.dto.external.ProcessingPeriodDto;
import mw.gov.health.lmis.reports.dto.external.ProcessingScheduleDto;
import mw.gov.health.lmis.reports.dto.external.ProgramDto;
import mw.gov.health.lmis.reports.exception.PermissionMessageException;
import mw.gov.health.lmis.reports.service.ViewPermissionService;
import mw.gov.health.lmis.reports.service.referencedata.GeographicZoneReferenceDataService;
import mw.gov.health.lmis.reports.service.referencedata.OrderableReferenceDataService;
import mw.gov.health.lmis.reports.service.referencedata.PeriodReferenceDataService;
import mw.gov.health.lmis.reports.service.referencedata.ProgramReferenceDataService;
import mw.gov.health.lmis.reports.service.stockmanagement.StockCardLineItemReasonDto;
import mw.gov.health.lmis.reports.service.stockmanagement.StockCardLineItemReasonStockmanagementService;
import mw.gov.health.lmis.testutils.StockCardLineItemReasonDtoDataBuilder;
import mw.gov.health.lmis.utils.Message;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

@SuppressWarnings("PMD.TooManyMethods")
public class ReportsControllerIntegrationTest extends BaseWebIntegrationTest {
  private static final String RESOURCE_URL = "/api/reports";
  private static final String CARD_SUMMARY_REPORT = RESOURCE_URL + "/stockCardSummaries/print";
  private static final String NAME = "name";
  private static final String CODE = "code";
  private static final String DESCRIPTION = "description";

  @MockBean
  private GeographicZoneReferenceDataService geographicZoneReferenceDataService;

  @MockBean
  private PeriodReferenceDataService periodReferenceDataService;

  @MockBean
  private ProgramReferenceDataService programReferenceDataService;

  @MockBean
  private StockCardLineItemReasonStockmanagementService reasonStockmanagementService;

  @MockBean
  private OrderableReferenceDataService orderableReferenceDataService;

  @MockBean
  private ViewPermissionService viewPermissionService;

  @Before
  public void setUp() {
    mockUserAuthenticated();
  }

  // GET /api/reports/districts

  @Test
  public void shouldGetAllDistricts() {
    // given
    GeographicZoneDto[] zones = { generateGeographicZone(), generateGeographicZone() };
    given(geographicZoneReferenceDataService.search(3, null)).willReturn(asList(zones));

    // when
    GeographicZoneDto[] result = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL + "/districts")
        .then()
        .statusCode(200)
        .extract().as(GeographicZoneDto[].class);

    // then
    assertNotNull(result);
    assertEquals(2, result.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // GET /api/reports/processingPeriods

  @Test
  public void shouldGetNonFutureProcessingPeriods() {
    // given
    ProcessingPeriodDto[] periods = { generateProcessingPeriod(), generateProcessingPeriod() };
    given(periodReferenceDataService.getNonFuturePeriods()).willReturn(asList(periods));

    // when
    ProcessingPeriodDto[] result = restAssured.given()
            .queryParam(ACCESS_TOKEN, getToken())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get(RESOURCE_URL + "/processingPeriods")
            .then()
            .statusCode(200)
            .extract().as(ProcessingPeriodDto[].class);

    // then
    assertNotNull(result);
    assertEquals(2, result.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // GET /api/reports/programs

  @Test
  public void shouldGetAllPrograms() {
    // given
    ProgramDto[] programs = { generateProgram(), generateProgram() };
    given(programReferenceDataService.findAll()).willReturn(asList(programs));

    // when
    ProgramDto[] result = restAssured.given()
            .queryParam(ACCESS_TOKEN, getToken())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get(RESOURCE_URL + "/programs")
            .then()
            .statusCode(200)
            .extract().as(ProgramDto[].class);

    // then
    assertNotNull(result);
    assertEquals(2, result.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // GET /api/reports/orderables/stockout

  @Test
  public void shouldGetAllOrderablesForStockOutRateReport() {
    // given
    OrderableDto[] orderables = {generateOrderable(), generateOrderable()};
    given(orderableReferenceDataService.findAll()).willReturn(Lists.newArrayList(orderables));

    // when
    OrderableDto[] result = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL + "/orderables/stockout")
        .then()
        .statusCode(200)
        .extract().as(OrderableDto[].class);

    // then
    List<OrderableDto> list = Lists.newArrayList(result);

    assertThat(list, is(notNullValue()));
    assertThat(list, hasSize(4));
    assertThat(list, hasItem(hasProperty("productCode", is("ALL_LA"))));
    assertThat(list, hasItem(hasProperty("productCode", is("ALL_IC"))));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllValidReasons() {
    List<StockCardLineItemReasonDto> reasons = asList(
        new StockCardLineItemReasonDtoDataBuilder().build(),
        new StockCardLineItemReasonDtoDataBuilder().build(),
        new StockCardLineItemReasonDtoDataBuilder()
            .withId(UUID.fromString("b5c27da7-bdda-4790-925a-9484c5dfb594")).build(),
        new StockCardLineItemReasonDtoDataBuilder()
            .withId(UUID.fromString("313f2f5f-0c22-4626-8c49-3554ef763de3")).build(),
        new StockCardLineItemReasonDtoDataBuilder()
            .withId(UUID.fromString("84eb13c3-3e54-4687-8a5f-a9f20dcd0dac")).build(),
        new StockCardLineItemReasonDtoDataBuilder()
            .withId(UUID.fromString("f8bb41e2-ab43-4781-ae7a-7bf3b5116b82")).build());
    given(reasonStockmanagementService.findAll()).willReturn(reasons);

    StockCardLineItemReasonDto[] result = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL + "/validReasons")
        .then()
        .statusCode(200)
        .extract().as(StockCardLineItemReasonDto[].class);

    // then
    assertNotNull(result);
    assertEquals(2, result.length);
    assertThat(result, arrayContainingInAnyOrder(reasons.get(0), reasons.get(1)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void return403WhenUserHasNoPermissionToViewStockCard() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();
    doThrow(new PermissionMessageException(new Message("key"))).when(viewPermissionService)
        .canViewStockCard(programId, facilityId);

    //when
    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam("program", programId.toString())
        .queryParam("facility", facilityId.toString())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(CARD_SUMMARY_REPORT)
        .then()
        .statusCode(403);
  }

  private GeographicZoneDto generateGeographicZone() {
    GeographicZoneDto zone = new GeographicZoneDto();
    zone.setId(UUID.randomUUID());
    zone.setCode(CODE);
    zone.setName(NAME);

    GeographicLevelDto level = new GeographicLevelDto();
    level.setId(UUID.randomUUID());
    level.setCode(CODE);
    level.setName(NAME);
    level.setLevelNumber(3);

    zone.setLevel(level);
    return zone;
  }

  private ProcessingPeriodDto generateProcessingPeriod() {
    ProcessingPeriodDto period = new ProcessingPeriodDto();
    period.setId(UUID.randomUUID());
    period.setName(NAME);
    period.setDescription(DESCRIPTION);
    period.setDurationInMonths(1);
    period.setStartDate(LocalDate.MIN);
    period.setEndDate(LocalDate.MAX);

    ProcessingScheduleDto schedule = new ProcessingScheduleDto();
    schedule.setId(UUID.randomUUID());
    schedule.setName(NAME);
    schedule.setCode(CODE);
    schedule.setDescription(DESCRIPTION);
    schedule.setModifiedDate(ZonedDateTime.now());
    period.setProcessingSchedule(schedule);

    return period;
  }

  private ProgramDto generateProgram() {
    ProgramDto program = new ProgramDto();
    program.setId(UUID.randomUUID());
    program.setCode(CODE);
    program.setName(NAME);
    program.setDescription(DESCRIPTION);
    program.setActive(true);
    program.setPeriodsSkippable(true);
    program.setShowNonFullSupplyTab(true);

    return program;
  }

  private OrderableDto generateOrderable() {
    OrderableDto dto = new OrderableDto();
    dto.setId(UUID.randomUUID());
    dto.setProductCode(CODE);
    dto.setFullProductName(NAME);

    return dto;
  }
}
