package mw.gov.health.lmis.reports.service;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.util.JRTextMeasurerUtil;
import org.springframework.web.servlet.view.jasperreports.JasperReportsMultiFormatView;

import java.util.Map;

/**
 * A multi-format view whose CSV fills skip text measurement. The CSV exporter writes each
 * value in full whatever the layout, so measuring where it would be cut only costs time; the
 * other formats draw the cut text and keep the normal measurer.
 */
public class MeasurementFreeCsvView extends JasperReportsMultiFormatView {

  private static final String CSV = "csv";

  @Override
  protected JasperPrint fillReport(Map<String, Object> model) throws Exception {
    if (CSV.equals(model.get(DEFAULT_FORMAT_KEY))) {
      getReport().setProperty(JRTextMeasurerUtil.PROPERTY_TEXT_MEASURER_FACTORY,
          OneLineTextMeasurerFactory.class.getName());
    }
    return super.fillReport(model);
  }
}
