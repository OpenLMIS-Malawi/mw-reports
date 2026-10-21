package mw.gov.health.lmis.reports.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.engine.util.JRStyledTextUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MeasurementFreeCsvViewTest {

  private static final String LONG_VALUE = String.join("",
      Collections.nCopies(4, "Amoxicillin 250mg dispersible tablet, pack of 100, "));

  private MeasurementFreeCsvView view;

  @Before
  public void setUp() throws Exception {
    view = new MeasurementFreeCsvView();
    view.setUrl(narrowCellReport().toURI().toString());
    view.setApplicationContext(new StaticApplicationContext());
  }

  @Test
  public void shouldFillCsvWithoutCuttingText() throws Exception {
    JRPrintText cell = fill("csv");

    assertEquals(LONG_VALUE, truncatedText(cell));
  }

  @Test
  public void shouldKeepCuttingTextForPdf() throws Exception {
    JRPrintText cell = fill("pdf");

    assertTrue(truncatedText(cell).length() < LONG_VALUE.length());
  }

  private JRPrintText fill(String format) throws Exception {
    Map<String, Object> model = new HashMap<>();
    model.put("format", format);
    model.put("datasource", new JREmptyDataSource(1));

    JasperPrint print = view.fillReport(model);

    return (JRPrintText) print.getPages().get(0).getElements().get(0);
  }

  private static String truncatedText(JRPrintText cell) {
    return JRStyledTextUtil.getInstance(DefaultJasperReportsContext.getInstance())
        .getTruncatedText(cell);
  }

  private static File narrowCellReport() throws Exception {
    JRDesignTextField cell = new JRDesignTextField();
    cell.setWidth(32);
    cell.setHeight(15);
    cell.setExpression(new JRDesignExpression("\"" + LONG_VALUE + "\""));

    JRDesignBand detail = new JRDesignBand();
    detail.setHeight(15);
    detail.addElement(cell);

    JasperDesign design = new JasperDesign();
    design.setName("narrowCell");
    ((JRDesignSection) design.getDetailSection()).addBand(detail);
    JasperReport report = JasperCompileManager.compileReport(design);

    File file = File.createTempFile("narrowCell", ".jasper");
    file.deleteOnExit();
    JRSaver.saveObject(report, file);
    return file;
  }
}
