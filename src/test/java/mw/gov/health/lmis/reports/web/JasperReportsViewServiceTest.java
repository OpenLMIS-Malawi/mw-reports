package mw.gov.health.lmis.reports.web;

import mw.gov.health.lmis.reports.domain.JasperTemplate;
import mw.gov.health.lmis.reports.dto.external.FacilityDto;
import mw.gov.health.lmis.reports.dto.external.OrderDto;
import mw.gov.health.lmis.reports.dto.external.ProcessingPeriodDto;
import mw.gov.health.lmis.reports.dto.external.ProcessingScheduleDto;
import mw.gov.health.lmis.reports.dto.external.ProgramDto;
import mw.gov.health.lmis.reports.service.JasperReportsViewService;
import mw.gov.health.lmis.reports.service.fulfillment.OrderService;
import mw.gov.health.lmis.reports.service.referencedata.PeriodReferenceDataService;
import mw.gov.health.lmis.reports.service.referencedata.ProgramReferenceDataService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static mw.gov.health.lmis.reports.web.ReportTypes.ORDER_REPORT;
import static org.mockito.Matchers.any;
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
}
