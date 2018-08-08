package mw.gov.health.lmis.reports.web;

import mw.gov.health.lmis.reports.dto.external.GeographicZoneDto;
import mw.gov.health.lmis.reports.dto.external.OrderableDto;
import mw.gov.health.lmis.reports.dto.external.ProcessingPeriodDto;
import mw.gov.health.lmis.reports.dto.external.ProgramDto;
import mw.gov.health.lmis.reports.dto.external.RequisitionDto;
import mw.gov.health.lmis.reports.exception.JasperReportViewException;
import mw.gov.health.lmis.reports.exception.NotFoundMessageException;
import mw.gov.health.lmis.reports.i18n.MessageKeys;
import mw.gov.health.lmis.reports.service.JasperReportsViewService;
import mw.gov.health.lmis.reports.service.PermissionService;
import mw.gov.health.lmis.reports.service.referencedata.GeographicZoneReferenceDataService;
import mw.gov.health.lmis.reports.service.referencedata.OrderableReferenceDataService;
import mw.gov.health.lmis.reports.service.referencedata.PeriodReferenceDataService;
import mw.gov.health.lmis.reports.service.referencedata.ProgramReferenceDataService;
import mw.gov.health.lmis.reports.service.requisition.RequisitionService;
import mw.gov.health.lmis.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Controller
@Transactional
@RequestMapping("/api/reports")
public class ReportsController extends BaseController {
  private static int DISTRICT_LEVEL = 3;

  @Autowired
  private GeographicZoneReferenceDataService geographicZoneReferenceDataService;

  @Autowired
  private PeriodReferenceDataService periodReferenceDataService;

  @Autowired
  private ProgramReferenceDataService programReferenceDataService;

  @Autowired
  private OrderableReferenceDataService orderableReferenceDataService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private JasperReportsViewService jasperReportsViewService;

  @Autowired
  private RequisitionService requisitionService;

  /**
   * Print out requisition as a PDF file.
   *
   * @param id The UUID of the requisition to print
   * @return ResponseEntity with the "#200 OK" HTTP response status and PDF file on success, or
   *     ResponseEntity containing the error description status.
   */
  @RequestMapping(value = "/requisitions/{id}/print", method = RequestMethod.GET)
  @ResponseBody
  public ModelAndView print(HttpServletRequest request, @PathVariable("id") UUID id)
          throws JasperReportViewException {
    RequisitionDto requisition = requisitionService.findOne(id);

    if (requisition == null) {
      throw new NotFoundMessageException(
              new Message(MessageKeys.ERROR_REQUISITION_NOT_FOUND, id));
    }
    permissionService.canViewRequisition(requisition);

    return jasperReportsViewService.getRequisitionJasperReportView(requisition, request);
  }

  /**
   * Get all districts.
   *
   * @return districts.
   */
  @RequestMapping(value = "/districts", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Collection<GeographicZoneDto> getDistricts() {
    permissionService.canViewReportsOrOrders();
    return geographicZoneReferenceDataService.search(DISTRICT_LEVEL, null);
  }

  /**
   * Get all processing periods.
   *
   * @return processing periods.
   */
  @RequestMapping(value = "/processingPeriods", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Collection<ProcessingPeriodDto> getProcessingPeriods() {
    permissionService.canViewReportsOrOrders();
    List<ProcessingPeriodDto> periods = periodReferenceDataService.findAll();
    Collections.reverse(periods);
    return periods;
  }

  /**
   * Get all programs.
   *
   * @return programs.
   */
  @RequestMapping(value = "/programs", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Collection<ProgramDto> getPrograms() {
    permissionService.canViewReportsOrOrders();
    return programReferenceDataService.findAll();
  }

  /**
   * Get all orderables for stock out rate report.
   *
   * @return programs.
   */
  @RequestMapping(value = "/orderables/stockout", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Collection<OrderableDto> getOrderables() {
    permissionService.canViewReportsOrOrders();

    List<OrderableDto> list = orderableReferenceDataService.findAll();
    list.add(new OrderableDto("ALL_LA", "All malaria formulations"));
    list.add(new OrderableDto("ALL_IC", "All implantable contraceptives"));

    return list;
  }
}
