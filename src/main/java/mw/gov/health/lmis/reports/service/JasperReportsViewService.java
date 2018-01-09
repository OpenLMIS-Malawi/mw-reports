package mw.gov.health.lmis.reports.service;

import static java.io.File.createTempFile;
import static mw.gov.health.lmis.reports.dto.external.TimelinessReportFacilityDto.DISTRICT_LEVEL;
import static mw.gov.health.lmis.reports.i18n.JasperMessageKeys.ERROR_JASPER_FILE_CREATION;
import static mw.gov.health.lmis.reports.i18n.MessageKeys.ERROR_IO;
import static mw.gov.health.lmis.reports.i18n.MessageKeys.ERROR_JASPER_FILE_FORMAT;
import static mw.gov.health.lmis.reports.i18n.ReportingMessageKeys.ERROR_REPORTING_CLASS_NOT_FOUND;
import static mw.gov.health.lmis.reports.i18n.ReportingMessageKeys.ERROR_REPORTING_IO;
import static mw.gov.health.lmis.reports.web.ReportTypes.ORDER_REPORT;
import static net.sf.jasperreports.engine.export.JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN;
import static org.apache.commons.io.FileUtils.writeByteArrayToFile;

import mw.gov.health.lmis.reports.dto.RequisitionReportDto;
import mw.gov.health.lmis.reports.dto.external.BasicRequisitionDto;
import mw.gov.health.lmis.reports.dto.external.FacilityDto;
import mw.gov.health.lmis.reports.dto.external.GeographicZoneDto;
import mw.gov.health.lmis.reports.dto.external.OrderDto;
import mw.gov.health.lmis.reports.dto.external.OrderLineItemDto;
import mw.gov.health.lmis.reports.dto.external.ProcessingPeriodDto;
import mw.gov.health.lmis.reports.dto.external.ProgramDto;
import mw.gov.health.lmis.reports.dto.external.RequisitionDto;
import mw.gov.health.lmis.reports.dto.external.RequisitionStatusDto;
import mw.gov.health.lmis.reports.dto.external.RequisitionTemplateColumnDto;
import mw.gov.health.lmis.reports.dto.external.RequisitionTemplateDto;
import mw.gov.health.lmis.reports.dto.external.TimelinessReportFacilityDto;
import mw.gov.health.lmis.reports.service.fulfillment.OrderService;
import mw.gov.health.lmis.reports.service.referencedata.BaseReferenceDataService;
import mw.gov.health.lmis.reports.service.referencedata.FacilityReferenceDataService;
import mw.gov.health.lmis.reports.service.referencedata.GeographicZoneReferenceDataService;
import mw.gov.health.lmis.reports.service.referencedata.PeriodReferenceDataService;
import mw.gov.health.lmis.reports.service.referencedata.ProgramReferenceDataService;
import mw.gov.health.lmis.reports.service.referencedata.UserReferenceDataService;
import mw.gov.health.lmis.reports.service.requisition.RequisitionService;
import mw.gov.health.lmis.reports.web.RequisitionReportDtoBuilder;
import mw.gov.health.lmis.utils.ReportUtils;
import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;

import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsMultiFormatView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import mw.gov.health.lmis.reports.domain.JasperTemplate;
import mw.gov.health.lmis.reports.exception.JasperReportViewException;

@Service
public class JasperReportsViewService {
  private static final String REQUISITION_REPORT_DIR = "/jasperTemplates/requisition.jrxml";
  private static final String REQUISITION_LINE_REPORT_DIR =
          "/jasperTemplates/requisitionLines.jrxml";
  private static final String DATASOURCE = "datasource";

  @Autowired
  private DataSource replicationDataSource;

  @Autowired
  private FacilityReferenceDataService facilityReferenceDataService;

  @Autowired
  private ProgramReferenceDataService programReferenceDataService;

  @Autowired
  private PeriodReferenceDataService periodReferenceDataService;

  @Autowired
  private GeographicZoneReferenceDataService geographicZoneReferenceDataService;

  @Autowired
  private RequisitionService requisitionService;

  @Autowired
  private OrderService orderService;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private RequisitionReportDtoBuilder requisitionReportDtoBuilder;

  /**
   * Create Jasper Report View.
   * Create Jasper Report (".jasper" file) from bytes from Template entity.
   * Set 'Jasper' exporter parameters, JDBC data source, web application context, url to file.
   *
   * @param jasperTemplate template that will be used to create a view
   * @param request  it is used to take web application context
   * @return created jasper view.
   * @throws JasperReportViewException if there will be any problem with creating the view.
   */
  public JasperReportsMultiFormatView getJasperReportsView(
      JasperTemplate jasperTemplate, HttpServletRequest request) throws JasperReportViewException {
    JasperReportsMultiFormatView jasperView = new JasperReportsMultiFormatView();
    setExportParams(jasperView);
    jasperView.setUrl(getReportUrlForReportData(jasperTemplate));
    jasperView.setJdbcDataSource(replicationDataSource);

    if (getApplicationContext(request) != null) {
      jasperView.setApplicationContext(getApplicationContext(request));
    }
    return jasperView;
  }

  /**
   * Set export parameters in jasper view.
   */
  private void setExportParams(JasperReportsMultiFormatView jasperView) {
    Map<JRExporterParameter, Object> reportFormatMap = new HashMap<>();
    reportFormatMap.put(IS_USING_IMAGES_TO_ALIGN, false);
    jasperView.setExporterParameters(reportFormatMap);
  }

  /**
   * Get application context from servlet.
   */
  public WebApplicationContext getApplicationContext(HttpServletRequest servletRequest) {
    ServletContext servletContext = servletRequest.getSession().getServletContext();
    return WebApplicationContextUtils.getWebApplicationContext(servletContext);
  }

  /**
   * Create ".jasper" file with byte array from Template.
   *
   * @return Url to ".jasper" file.
   */
  private String getReportUrlForReportData(JasperTemplate jasperTemplate)
      throws JasperReportViewException {
    File tmpFile;

    try {
      tmpFile = createTempFile(jasperTemplate.getName() + "_temp", ".jasper");
    } catch (IOException exp) {
      throw new JasperReportViewException(
          exp, ERROR_JASPER_FILE_CREATION
      );
    }

    try (ObjectInputStream inputStream =
             new ObjectInputStream(new ByteArrayInputStream(jasperTemplate.getData()))) {
      JasperReport jasperReport = (JasperReport) inputStream.readObject();

      try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
           ObjectOutputStream out = new ObjectOutputStream(bos)) {

        out.writeObject(jasperReport);
        writeByteArrayToFile(tmpFile, bos.toByteArray());

        return tmpFile.toURI().toURL().toString();
      }
    } catch (IOException exp) {
      throw new JasperReportViewException(exp, ERROR_REPORTING_IO, exp.getMessage());
    } catch (ClassNotFoundException exp) {
      throw new JasperReportViewException(
          exp, ERROR_REPORTING_CLASS_NOT_FOUND, JasperReport.class.getName());
    }
  }

  /**
   * Get customized Jasper Report View for Timeliness Report.
   *
   * @param jasperView generic jasper report view
   * @param parameters template parameters populated with values from the request
   * @return customized jasper view.
   */
  public ModelAndView getTimelinessJasperReportView(JasperReportsMultiFormatView jasperView,
                                                    Map<String, Object> parameters) {
    ProgramDto program = programReferenceDataService.findOne(
            UUID.fromString(parameters.get("program").toString())
    );
    ProcessingPeriodDto period = periodReferenceDataService.findOne(
            UUID.fromString(parameters.get("period").toString())
    );
    GeographicZoneDto district = null;
    Object districtId = parameters.get("district");
    if (districtId != null && !districtId.toString().isEmpty()) {
      district = geographicZoneReferenceDataService.findOne(
              UUID.fromString(districtId.toString()));
    }
    List<FacilityDto> facilities = getFacilitiesForTimelinessReport(program, period, district);

    parameters.put(DATASOURCE, new JRBeanCollectionDataSource(facilities));
    parameters.put("program", program);
    parameters.put("period", period);
    parameters.put("district", district);

    return new ModelAndView(jasperView, parameters);
  }

  /**
   * Get customized Jasper Report View for Order Report.
   *
   * @param jasperView generic jasper report view
   * @param parameters template parameters populated with values from the request
   * @return customized jasper view.
   */
  public ModelAndView getOrderJasperReportView(JasperReportsMultiFormatView jasperView,
                                               Map<String, Object> parameters) {
    OrderDto order = orderService.findOne(
            UUID.fromString(parameters.get("order").toString())
    );
    order.getStatusChanges().forEach(
        statusChange -> statusChange.setAuthor(
            getIfPresent(userReferenceDataService, statusChange.getAuthorId()))
    );
    List<OrderLineItemDto> items = order.getOrderLineItems();
    items.sort(Comparator.comparing(c -> c.getOrderable().getProductCode()));

    parameters.put(DATASOURCE, new JRBeanCollectionDataSource(items));
    parameters.put("order", order);

    return new ModelAndView(jasperView, parameters);
  }

  /**
   * Create custom Jasper Report View for printing a requisition.
   *
   * @param requisition requisition to render report for.
   * @param request  it is used to take web application context.
   * @return created jasper view.
   * @throws JasperReportViewException if there will be any problem with creating the view.
   */
  public ModelAndView getRequisitionJasperReportView(
          RequisitionDto requisition, HttpServletRequest request) throws JasperReportViewException {
    RequisitionReportDto reportDto = requisitionReportDtoBuilder.build(requisition);
    RequisitionTemplateDto template = requisition.getTemplate();

    Map<String, Object> params = ReportUtils.createParametersMap();
    params.put("subreport", createCustomizedRequisitionLineSubreport(template));
    params.put(DATASOURCE, Collections.singletonList(reportDto));
    params.put("template", template);

    JasperReportsMultiFormatView jasperView = new JasperReportsMultiFormatView();
    setExportParams(jasperView);
    setCustomizedJasperTemplateForRequisitionReport(jasperView);

    if (getApplicationContext(request) != null) {
      jasperView.setApplicationContext(getApplicationContext(request));
    }
    return new ModelAndView(jasperView, params);
  }

  /**
   * Get report's filename.
   *
   * @param template jasper template
   * @param params template parameters populated with values from the request
   * @return filename
   */
  public String getFilename(JasperTemplate template, Map<String, Object> params) {
    String templateType = template.getType();
    StringBuilder fileName = new StringBuilder(template.getName());
    Collection<Object> values = params.values();
    if (ORDER_REPORT.equals(templateType)) {
      OrderDto order = orderService.findOne(
          UUID.fromString(params.get("order").toString())
      );
      values = Arrays.asList(
          order.getProgram().getName(),
          order.getProcessingPeriod().getName(),
          order.getFacility().getName()
      );
    }
    for (Object value : values) {
      fileName
          .append('_')
          .append(value.toString());
    }
    return fileName.toString()
        .replaceAll("\\s+", "_")
        .toLowerCase(Locale.ENGLISH);
  }

  private JasperDesign createCustomizedRequisitionLineSubreport(RequisitionTemplateDto template)
          throws JasperReportViewException {
    try (InputStream inputStream = getClass().getResourceAsStream(REQUISITION_LINE_REPORT_DIR)) {
      JasperDesign design = JRXmlLoader.load(inputStream);
      JRBand detail = design.getDetailSection().getBands()[0];
      JRBand header = design.getColumnHeader();

      Map<String, RequisitionTemplateColumnDto> columns =
              ReportUtils.getSortedTemplateColumnsForPrint(template.getColumnsMap());

      ReportUtils.customizeBandWithTemplateFields(detail, columns, design.getPageWidth(), 9);
      ReportUtils.customizeBandWithTemplateFields(header, columns, design.getPageWidth(), 9);

      return design;
    } catch (IOException err) {
      throw new JasperReportViewException(err, ERROR_IO, err.getMessage());
    } catch (JRException err) {
      throw new JasperReportViewException(err, ERROR_JASPER_FILE_FORMAT, err.getMessage());
    }
  }

  private void setCustomizedJasperTemplateForRequisitionReport(
          JasperReportsMultiFormatView jasperView) throws JasperReportViewException {
    try (InputStream inputStream = getClass().getResourceAsStream(REQUISITION_REPORT_DIR)) {
      File reportTempFile = createTempFile("requisitionReport_temp", ".jasper");
      JasperReport report = JasperCompileManager.compileReport(inputStream);

      try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
           ObjectOutputStream out = new ObjectOutputStream(bos)) {

        out.writeObject(report);
        writeByteArrayToFile(reportTempFile, bos.toByteArray());

        jasperView.setUrl(reportTempFile.toURI().toURL().toString());
      }
    } catch (IOException err) {
      throw new JasperReportViewException(err, ERROR_IO, err.getMessage());
    } catch (JRException err) {
      throw new JasperReportViewException(err, ERROR_JASPER_FILE_FORMAT, err.getMessage());
    }
  }

  private <T> T getIfPresent(BaseReferenceDataService<T> service, UUID id) {
    return Optional.ofNullable(id).isPresent() ? service.findOne(id) : null;
  }

  private List<FacilityDto> getFacilitiesForTimelinessReport(
          ProgramDto program, ProcessingPeriodDto processingPeriod, GeographicZoneDto district) {
    Set<RequisitionStatusDto> validStatuses = Arrays.stream(RequisitionStatusDto.values())
            .filter(RequisitionStatusDto::isApproved)
            .collect(Collectors.toSet());

    List<FacilityDto> facilities;
    if (district != null) {
      facilities = facilityReferenceDataService.search(null, null, district.getId(), true)
              .getContent();
    } else {
      facilities = facilityReferenceDataService.findAll();
    }

    List<TimelinessReportFacilityDto> facilitiesMissingRnR = new ArrayList<>();
    // find active facilities that are missing R&R
    for (FacilityDto facility : facilities) {
      if (facility.getActive()) {
        Page<BasicRequisitionDto> requisitions = requisitionService.search(
                facility.getId(), program.getId(), null, null, processingPeriod.getId(),
                null, validStatuses, null);
        if (requisitions.getTotalElements() == 0) {
          TimelinessReportFacilityDto timelinessFacility = new TimelinessReportFacilityDto();
          facility.export(timelinessFacility);
          facilitiesMissingRnR.add(timelinessFacility);
        }
      }
    }

    // sort alphabetically by district and then facility name
    Comparator<FacilityDto> comparator = Comparator.comparing(
        facility -> facility.getZoneByLevelNumber(DISTRICT_LEVEL).getName());
    comparator = comparator.thenComparing(Comparator.comparing(FacilityDto::getName));

    return facilitiesMissingRnR.stream()
            .sorted(comparator).collect(Collectors.toList());
  }
}
