package mw.gov.health.lmis.reports.web;

import static java.lang.String.join;
import static java.util.Arrays.asList;

import static mw.gov.health.lmis.reports.i18n.JasperMessageKeys.ERROR_REPORTING_TEMPLATE_NOT_FOUND_WITH_NAME;
import static mw.gov.health.lmis.reports.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_FORMAT_NOT_ALLOWED;
import static net.sf.jasperreports.engine.JRParameter.REPORT_LOCALE;
import static net.sf.jasperreports.engine.JRParameter.REPORT_RESOURCE_BUNDLE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import mw.gov.health.lmis.reports.domain.JasperTemplate;
import mw.gov.health.lmis.reports.dto.external.GeographicZoneDto;
import mw.gov.health.lmis.reports.dto.external.OrderableDto;
import mw.gov.health.lmis.reports.dto.external.PhysicalInventoryDto;
import mw.gov.health.lmis.reports.dto.external.ProcessingPeriodDto;
import mw.gov.health.lmis.reports.dto.external.ProgramDto;
import mw.gov.health.lmis.reports.dto.external.ProofOfDeliveryDto;
import mw.gov.health.lmis.reports.dto.external.RequisitionDto;
import mw.gov.health.lmis.reports.exception.JasperReportViewException;
import mw.gov.health.lmis.reports.exception.NotFoundMessageException;
import mw.gov.health.lmis.reports.exception.ProofOfDeliveryNotFoundException;
import mw.gov.health.lmis.reports.exception.ValidationMessageException;
import mw.gov.health.lmis.reports.i18n.MessageKeys;
import mw.gov.health.lmis.reports.repository.JasperTemplateRepository;
import mw.gov.health.lmis.reports.service.JasperReportsViewService;
import mw.gov.health.lmis.reports.service.PermissionService;
import mw.gov.health.lmis.reports.service.ViewPermissionService;
import mw.gov.health.lmis.reports.service.fulfillment.ProofOfDeliveryDataService;
import mw.gov.health.lmis.reports.service.referencedata.GeographicZoneReferenceDataService;
import mw.gov.health.lmis.reports.service.referencedata.OrderableReferenceDataService;
import mw.gov.health.lmis.reports.service.referencedata.PeriodReferenceDataService;
import mw.gov.health.lmis.reports.service.referencedata.PhysicalInventoryReferenceDataService;
import mw.gov.health.lmis.reports.service.referencedata.ProgramReferenceDataService;
import mw.gov.health.lmis.reports.service.requisition.RequisitionService;
import mw.gov.health.lmis.reports.service.stockmanagement.StockCardLineItemReasonDto;
import mw.gov.health.lmis.reports.service.stockmanagement.StockCardLineItemReasonStockmanagementService;
import mw.gov.health.lmis.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsMultiFormatView;

@Controller
@Transactional
@RequestMapping("/api/reports")
public class ReportsController extends BaseController {

  private static int DISTRICT_LEVEL = 3;
  public static final String PRINT_PI = "Print PI";
  public static final String FORMAT = "format";
  public static final String POD_REPORT_URL = "/jasperTemplates/proofOfDelivery.jrxml";

  @Autowired
  private GeographicZoneReferenceDataService geographicZoneReferenceDataService;

  @Autowired
  private PeriodReferenceDataService periodReferenceDataService;

  @Autowired
  private ProgramReferenceDataService programReferenceDataService;

  @Autowired
  private StockCardLineItemReasonStockmanagementService reasonStockmanagementService;

  @Autowired
  private PhysicalInventoryReferenceDataService physicalInventoryReferenceDataService;

  @Autowired
  private OrderableReferenceDataService orderableReferenceDataService;

  @Autowired
  private ProofOfDeliveryDataService proofOfDeliveryDataService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private ViewPermissionService viewPermissionService;

  @Autowired
  private JasperReportsViewService jasperReportsViewService;

  @Autowired
  private JasperTemplateRepository jasperTemplateRepository;

  @Autowired
  private RequisitionService requisitionService;

  @Value("${dateTimeFormat}")
  private String dateTimeFormat;

  @Value("${dateFormat}")
  private String dateFormat;

  @Value("${time.zoneId}")
  private String timeZoneId;

  @Value("${groupingSeparator}")
  private String groupingSeparator;

  @Value("${groupingSize}")
  private String groupingSize;

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
    viewPermissionService.canViewRequisition(requisition);

    return jasperReportsViewService.getRequisitionJasperReportView(requisition, request);
  }

  /**
   * Print out physical inventory as a PDF file.
   *
   * @param id The UUID of the stock event to print
   * @param format The report format
   * @return ResponseEntity with the "#200 OK" HTTP response status and PDF file on success, or
   *     ResponseEntity containing the error description status.
   */
  @GetMapping(value = "physicalInventories/{id}", params = FORMAT)
  @ResponseBody
  public ModelAndView print(@PathVariable("id") UUID id, @RequestParam String format)
      throws JasperReportViewException {

    checkFormat(format.toLowerCase());

    JasperTemplate printTemplate = jasperTemplateRepository.findByName(PRINT_PI);
    if (printTemplate == null) {
      throw new ValidationMessageException(
          new Message(ERROR_REPORTING_TEMPLATE_NOT_FOUND_WITH_NAME, PRINT_PI));
    }

    JasperReportsMultiFormatView jasperView =
        jasperReportsViewService.getJasperReportsView(printTemplate);

    return new ModelAndView(jasperView, getParams(id, format));
  }

  /**
   * Prints proofOfDelivery in PDF format.
   *
   * @param id UUID of ProofOfDelivery to print
   */
  @RequestMapping(value = "/proofsOfDelivery/{id}/print", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  public ModelAndView printProofOfDelivery(HttpServletRequest request,
                                           @PathVariable("id") UUID id,
                                           OAuth2Authentication authentication)
      throws JasperReportViewException {

    ProofOfDeliveryDto proofOfDelivery = findProofOfDelivery(id);

    Map<String, Object> params = new HashMap<>();
    params.put(FORMAT, "pdf");
    params.put("id", proofOfDelivery.getId());
    params.put("dateFormat", dateFormat);
    DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    decimalFormatSymbols.setGroupingSeparator(groupingSeparator.charAt(0));
    DecimalFormat decimalFormat = new DecimalFormat("", decimalFormatSymbols);
    decimalFormat.setGroupingSize(Integer.parseInt(groupingSize));
    params.put("decimalFormat", decimalFormat);
    params.put("dateTimeFormat", dateTimeFormat);
    params.put("timeZoneId", timeZoneId);

    return jasperReportsViewService
        .generateReport(POD_REPORT_URL, params);
  }

  /**
   * Get stock card summaries report by program and facility.
   *
   * @return generated PDF report
   */
  @RequestMapping(value = "/stockCardSummaries/print", method = GET)
  @ResponseBody
  public ModelAndView getStockCardSummaries(
      @RequestParam("program") UUID program,
      @RequestParam("facility") UUID facility) throws JasperReportViewException {
    viewPermissionService.canViewStockCard(program, facility);
    return jasperReportsViewService.getStockCardSummariesReportView(program, facility);
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
    viewPermissionService.canViewReportsOrOrders();
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
    viewPermissionService.canViewReportsOrOrders();
    return periodReferenceDataService.getNonFuturePeriods();
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
    viewPermissionService.canViewReportsOrOrders();
    return programReferenceDataService.findAll();
  }

  /**
   * Get all visible valid reasons.
   *
   * @return reasons.
   */
  @RequestMapping(value = "/validReasons", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Set<StockCardLineItemReasonDto> getValidReasons() {
    viewPermissionService.canViewReportsOrOrders();

    List<UUID> disabledReasons = asList(
        UUID.fromString("b5c27da7-bdda-4790-925a-9484c5dfb594"), // Consumed
        UUID.fromString("313f2f5f-0c22-4626-8c49-3554ef763de3"), // Receipts
        UUID.fromString("84eb13c3-3e54-4687-8a5f-a9f20dcd0dac"), // Beginning Balance Excess
        UUID.fromString("f8bb41e2-ab43-4781-ae7a-7bf3b5116b82")); // Beginning Balance Insufficiency

    return reasonStockmanagementService.findAll().stream()
        .filter(reason -> !disabledReasons.contains(reason.getId()))
        .collect(Collectors.toSet());
  }

  /**
   * Get all orderables for stock out rate report.
   *
   * @return programs.
   */
  @RequestMapping(value = "/orderables/stockout", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Collection<OrderableDto> getOrderables(
      @RequestParam(value = "program", required = false) String programName) {
    viewPermissionService.canViewReportsOrOrders();

    List<OrderableDto> list = null;

    if (null != programName) {
      Collection<ProgramDto> programs = programReferenceDataService.search(programName);

      if (!programs.isEmpty()) {
        list = orderableReferenceDataService
            .findByProgramCode(programs.iterator().next().getCode());
      }
    }

    if (null == list) {
      list = orderableReferenceDataService.findAll();
    }

    if (null == programName || "malaria".equalsIgnoreCase(programName)) {
      list.add(new OrderableDto("ALL_LA", "All malaria formulations"));
    }

    if (null == programName || "rh".equalsIgnoreCase(programName)) {
      list.add(new OrderableDto("ALL_IC", "All implantable contraceptives"));
    }

    return list;
  }

  private Map<String, Object> getParams(UUID eventId, String format)
      throws JasperReportViewException {
    Map<String, Object> params = createParametersMap();
    String formatId = "'" + eventId + "'";
    DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    decimalFormatSymbols.setGroupingSeparator(groupingSeparator.charAt(0));
    DecimalFormat decimalFormat = new DecimalFormat("", decimalFormatSymbols);
    decimalFormat.setGroupingSize(Integer.parseInt(groupingSize));
    params.put("pi_id", formatId);
    params.put("dateTimeFormat", dateTimeFormat);
    params.put("dateFormat", dateFormat);
    params.put("timeZoneId", timeZoneId);
    params.put(FORMAT, format);
    params.put("decimalFormat", decimalFormat);
    params.put("subreport",
        jasperReportsViewService.createCustomizedPhysicalInventoryLineSubreport());

    return params;
  }

  /**
   * Set parameters of rendered pdf report.
   */
  public static Map<String, Object> createParametersMap() {
    Map<String, Object> params = new HashMap<>();
    params.put(FORMAT, "pdf");

    Locale currentLocale = LocaleContextHolder.getLocale();
    params.put(REPORT_LOCALE, currentLocale);

    ResourceBundle resourceBundle = ResourceBundle.getBundle("messages", currentLocale);
    params.put(REPORT_RESOURCE_BUNDLE, resourceBundle);

    return params;
  }

  private void checkPermission(UUID id) {
    PhysicalInventoryDto pi = physicalInventoryReferenceDataService.findById(id);
    permissionService.canEditPhysicalInventory(
        pi.getProgramId(),
        pi.getFacilityId());
  }

  private void checkFormat(String format) {
    List<String> supportedFormats = Arrays.asList("csv", "html", "pdf", "xls", "xlsx");
    if (!supportedFormats.contains(format)) {
      throw new ResourceNotFoundException(
          ERROR_PHYSICAL_INVENTORY_FORMAT_NOT_ALLOWED
              + format
              + join(", ", supportedFormats));
    }
  }

  private ProofOfDeliveryDto findProofOfDelivery(UUID id) {
    ProofOfDeliveryDto entity = proofOfDeliveryDataService.findProofOfDelivery(id);
    if (null == entity) {
      throw new ProofOfDeliveryNotFoundException(id);
    }

    return entity;
  }
}
