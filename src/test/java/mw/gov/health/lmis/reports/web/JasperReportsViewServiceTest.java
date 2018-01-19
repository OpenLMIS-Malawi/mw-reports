package mw.gov.health.lmis.reports.web;

import mw.gov.health.lmis.reports.domain.JasperTemplate;
import mw.gov.health.lmis.reports.dto.external.FacilityDto;
import mw.gov.health.lmis.reports.dto.external.GeographicLevelDto;
import mw.gov.health.lmis.reports.dto.external.GeographicZoneDto;
import mw.gov.health.lmis.reports.dto.external.OrderDto;
import mw.gov.health.lmis.reports.dto.external.ProcessingPeriodDto;
import mw.gov.health.lmis.reports.dto.external.ProcessingScheduleDto;
import mw.gov.health.lmis.reports.dto.external.ProgramDto;
import mw.gov.health.lmis.reports.service.JasperReportsViewService;
import mw.gov.health.lmis.reports.service.fulfillment.OrderService;
import mw.gov.health.lmis.reports.service.referencedata.FacilityReferenceDataService;
import mw.gov.health.lmis.reports.service.referencedata.GeographicZoneReferenceDataService;
import mw.gov.health.lmis.reports.service.referencedata.PeriodReferenceDataService;
import mw.gov.health.lmis.reports.service.referencedata.ProgramReferenceDataService;
import mw.gov.health.lmis.reports.service.requisition.RequisitionService;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsMultiFormatView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static mw.gov.health.lmis.reports.dto.external.FacilityDto.DISTRICT_LEVEL;
import static mw.gov.health.lmis.reports.web.ReportTypes.AGGREGATE_ORDERS_REPORT;
import static mw.gov.health.lmis.reports.web.ReportTypes.ORDER_REPORT;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class JasperReportsViewServiceTest {

  @InjectMocks
  private JasperReportsViewService service;

  @Mock
  private ProgramReferenceDataService programReferenceDataService;

  @Mock
  private PeriodReferenceDataService periodReferenceDataService;

  @Mock
  private FacilityReferenceDataService facilityReferenceDataService;

  @Mock
  private GeographicZoneReferenceDataService geographicZoneReferenceDataService;

  @Mock
  private RequisitionService requisitionService;

  @Mock
  private ProgramDto program;

  @Mock
  private OrderDto order;

  @Mock
  private FacilityDto facility;

  @Mock
  private OrderService orderService;

  @Mock
  private ProcessingScheduleDto processingSchedule;

  private ProcessingPeriodDto period = new ProcessingPeriodDto();
  private ProcessingPeriodDto nextPeriod = new ProcessingPeriodDto();
  private List<ProcessingPeriodDto> periods = Arrays.asList(period, nextPeriod);

  private UUID programId = UUID.randomUUID();
  private UUID periodId = UUID.randomUUID();
  private UUID orderId = UUID.randomUUID();

  private Map<String, Object> reportParams = new HashMap<>();

  @Before
  public void setUp() {
    when(program.getId()).thenReturn(programId);
    when(order.getId()).thenReturn(orderId);

    period.setId(periodId);
    period.setName("Nov2017");
    period.setProcessingSchedule(processingSchedule);
    period.setStartDate(LocalDate.of(2017, 11, 1));
    period.setEndDate(LocalDate.of(2017, 11, 30));

    nextPeriod.setName("Dec2017");
    nextPeriod.setStartDate(LocalDate.of(2017, 12, 1));
    nextPeriod.setEndDate(LocalDate.of(2017, 12, 31));

    when(program.getName()).thenReturn("Essential Meds");
    when(facility.getName()).thenReturn("Chitipa DHO");

    when(programReferenceDataService.findOne(programId)).thenReturn(program);
    when(periodReferenceDataService.findOne(periodId)).thenReturn(period);
    when(periodReferenceDataService.findAll()).thenReturn(periods);
    when(periodReferenceDataService.search(any(), any())).thenReturn(periods);
    when(orderService.findOne(orderId)).thenReturn(order);

    when(order.getProgram()).thenReturn(program);
    when(order.getProcessingPeriod()).thenReturn(period);
    when(order.getFacility()).thenReturn(facility);

    reportParams.put("program", programId.toString());
    reportParams.put("period", periodId.toString());
  }

  @Test
  public void shouldGetTimelinessReportViewWithActiveFacilitiesMissingRnR() {
    // given
    List<FacilityDto> facilitiesToReturn = new ArrayList<>();

    // active facilities missing RnR
    FacilityDto facility = mockFacility(true, true);
    FacilityDto anotherFacility = mockFacility(true, true);
    facilitiesToReturn.add(facility);
    facilitiesToReturn.add(anotherFacility);

    // "on time" active facility
    facilitiesToReturn.add(mockFacility(true, false));

    // inactive facilities
    facilitiesToReturn.add(mockFacility(false, false));
    facilitiesToReturn.add(mockFacility(false, true));

    when(facilityReferenceDataService.findAll()).thenReturn(facilitiesToReturn);

    // when
    ModelAndView view = service.getTimelinessJasperReportView(
        new JasperReportsMultiFormatView(), reportParams);
    List<FacilityDto> facilities = extractFacilitiesFromOutputParams(view.getModel());

    // then
    Assert.assertEquals(2, facilities.size());
    List<UUID> facilityIds = facilities.stream()
        .map(FacilityDto::getId).collect(Collectors.toList());
    Assert.assertTrue(facilityIds.contains(facility.getId()));
    Assert.assertTrue(facilityIds.contains(anotherFacility.getId()));
  }

  @Test
  public void shouldGetTimelinessReportViewWithFacilitiesFromSpecifiedDistrict() {
    //given
    UUID districtId = UUID.randomUUID();
    reportParams.put("district", districtId.toString());

    // active facilities missing RnR
    FacilityDto facility = mockFacility(true, true, districtId, "parent-zone", "f1");
    FacilityDto childFacility = mockFacility(true, true, UUID.randomUUID(), "child-zone", "f2");

    GeographicLevelDto childLevel = mock(GeographicLevelDto.class);
    when(childLevel.getLevelNumber()).thenReturn(DISTRICT_LEVEL);

    GeographicZoneDto childZone = childFacility.getGeographicZone();
    childZone.setParent(facility.getGeographicZone());
    childZone.setLevel(childLevel);

    // facility missing RnR from another district
    mockFacility(true, true);

    List<FacilityDto> result = Arrays.asList(facility, childFacility);
    Page searchResult = mock(Page.class);
    when(searchResult.getTotalElements()).thenReturn(Long.valueOf(result.size()));
    when(searchResult.getContent()).thenReturn(result);

    when(facilityReferenceDataService.search(any(), any(), eq(districtId), eq(true)))
        .thenReturn(searchResult);

    // when
    ModelAndView view = service.getTimelinessJasperReportView(
        new JasperReportsMultiFormatView(), reportParams);
    List<FacilityDto> facilities = extractFacilitiesFromOutputParams(view.getModel());

    // then
    Assert.assertEquals(2, facilities.size());
    List<UUID> facilityIds = facilities.stream()
        .map(FacilityDto::getId).collect(Collectors.toList());
    Assert.assertTrue(facilityIds.contains(facility.getId()));
    Assert.assertTrue(facilityIds.contains(childFacility.getId()));
  }

  @Test
  public void shouldGetTimelinessReportViewWithFacilitiesFromAllZonesIfDistrictNotSpecified() {
    //given
    UUID zone1Id = UUID.randomUUID();
    UUID zone2Id = UUID.randomUUID();

    List<FacilityDto> facilitiesToReturn = Arrays.asList(
        // active facilities missing RnR from different zones
        mockFacility(true, true, zone1Id, "district A", "f1"),
        mockFacility(true, true, zone1Id, "district A", "f2"),
        mockFacility(true, true, zone2Id, "district B", "f3"),
        mockFacility(true, true, zone2Id, "district B", "f4")
    );

    when(facilityReferenceDataService.findAll()).thenReturn(facilitiesToReturn);

    // when
    ModelAndView view = service.getTimelinessJasperReportView(
        new JasperReportsMultiFormatView(), reportParams);
    List<FacilityDto> facilities = extractFacilitiesFromOutputParams(view.getModel());

    // then
    Assert.assertEquals(4, facilities.size());
    List<UUID> facilityIds = facilities.stream()
        .map(FacilityDto::getId).collect(Collectors.toList());
    for (FacilityDto facility : facilitiesToReturn) {
      Assert.assertTrue(facilityIds.contains(facility.getId()));
    }
  }

  @Test
  public void shouldSortFacilitiesReturnedWithTimelinessReportView() {
    //given
    UUID zone1Id = UUID.randomUUID();
    UUID zone2Id = UUID.randomUUID();

    FacilityDto facility1A = mockFacility(true, true, zone1Id, "zone1", "facilityA");
    FacilityDto facility1B = mockFacility(true, true, zone1Id, "zone1", "facilityB");
    FacilityDto facility2A = mockFacility(true, true, zone2Id, "zone2", "facilityA");
    FacilityDto facility2B = mockFacility(true, true, zone2Id, "zone2", "facilityB");

    when(facilityReferenceDataService.findAll()).thenReturn(Arrays.asList(
        facility2B, facility2A, facility1A, facility1B));

    // when
    ModelAndView view = service.getTimelinessJasperReportView(
        new JasperReportsMultiFormatView(), reportParams);
    List<FacilityDto> facilities = extractFacilitiesFromOutputParams(view.getModel());

    // then
    Assert.assertEquals(4, facilities.size());
    Assert.assertEquals(facility1A.getId(), facilities.get(0).getId());
    Assert.assertEquals(facility1B.getId(), facilities.get(1).getId());
    Assert.assertEquals(facility2A.getId(), facilities.get(2).getId());
    Assert.assertEquals(facility2B.getId(), facilities.get(3).getId());
  }

  @Test
  public void shouldGetFilenameWithProvidedParameters() {
    // given
    JasperTemplate template = new JasperTemplate();
    template.setName("Some report");

    Map<String, Object> params = new LinkedHashMap<>();
    params.put("program", "Essential Meds");
    params.put("period", "Jul2017");
    params.put("district", "Chitipa");

    // when
    String filename = service.getFilename(template, params);

    // then
    Assert.assertEquals("some_report_essential_meds_jul2017_chitipa", filename);
  }

  @Test
  public void shouldGetFilenameForOrderReport() {
    // given
    JasperTemplate template = new JasperTemplate();
    template.setName("Order");
    template.setType(ORDER_REPORT);

    Map<String, Object> params = new HashMap<>();
    params.put("order", orderId);
    // when
    String filename = service.getFilename(template, params);

    // then
    Assert.assertEquals("order_essential_meds_dec2017_chitipa_dho", filename);
  }

  @Test
  public void shouldGetFilenameForEmergencyOrderReport() {
    // given
    JasperTemplate template = new JasperTemplate();
    template.setName("Order");
    template.setType(ORDER_REPORT);
    when(order.getEmergency()).thenReturn(true);

    Map<String, Object> params = new HashMap<>();
    params.put("order", orderId);
    // when
    String filename = service.getFilename(template, params);

    // then
    Assert.assertEquals("order_essential_meds_nov2017_chitipa_dho", filename);
  }

  @Test
  public void shouldGetFilenameForAggregateOrdersReport() {
    // given
    JasperTemplate template = new JasperTemplate();
    template.setName("Health Centre Orders");
    template.setType(AGGREGATE_ORDERS_REPORT);

    Map<String, Object> params = new HashMap<>();
    params.put("program", "Essential Meds");
    params.put("period", "Nov2017");
    params.put("district", "Chitipa");
    // when
    String filename = service.getFilename(template, params);

    // then
    Assert.assertEquals("health_centre_orders_dec2017_chitipa_essential_meds", filename);
  }

  private List<FacilityDto> extractFacilitiesFromOutputParams(Map<String, Object> outputParams) {
    JRBeanCollectionDataSource datasource =
        (JRBeanCollectionDataSource) outputParams.get("datasource");
    return (List<FacilityDto>) datasource.getData();
  }

  private FacilityDto mockFacility(boolean isActive, boolean isMissingRnR) {
    return mockFacility(isActive, isMissingRnR, UUID.randomUUID(), "test", "test");
  }

  private FacilityDto mockFacility(boolean isActive, boolean isMissingRnR, UUID districtId,
                                   String districtName, String facilityName) {
    FacilityDto facility = new FacilityDto();

    UUID facilityId = UUID.randomUUID();
    facility.setId(facilityId);
    facility.setActive(isActive);
    facility.setName(facilityName);

    GeographicZoneDto geographicZoneDto = mock(GeographicZoneDto.class);
    when(geographicZoneDto.getId()).thenReturn(districtId);
    when(geographicZoneDto.getName()).thenReturn(districtName);

    GeographicLevelDto geographicLevelDto = mock(GeographicLevelDto.class);
    when(geographicLevelDto.getLevelNumber()).thenReturn(DISTRICT_LEVEL);
    when(geographicZoneDto.getLevel()).thenReturn(geographicLevelDto);
    when(geographicZoneReferenceDataService.findOne(districtId)).thenReturn(geographicZoneDto);

    facility.setGeographicZone(geographicZoneDto);

    Page requisitionSearchResult = mock(Page.class);
    when(requisitionSearchResult.getTotalElements()).thenReturn(
        (isMissingRnR) ? 0L : 1L);

    when(requisitionService.search(eq(facilityId), eq(programId), any(), any(),
        eq(periodId), any(), any(), any()))
        .thenReturn(requisitionSearchResult);
    return facility;
  }
}
