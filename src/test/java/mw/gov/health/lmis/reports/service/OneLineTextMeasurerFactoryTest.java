package mw.gov.health.lmis.reports.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.fill.JRMeasuredText;
import net.sf.jasperreports.engine.fill.JRTextMeasurer;
import net.sf.jasperreports.engine.util.JRStyledText;
import org.junit.Test;

import java.util.Collections;

public class OneLineTextMeasurerFactoryTest {

  private static final String LONG_VALUE = String.join("",
      Collections.nCopies(4, "Amoxicillin 250mg dispersible tablet, pack of 100, "));

  private final OneLineTextMeasurerFactory factory = new OneLineTextMeasurerFactory();

  @Test
  public void shouldReportWholeTextAsFittingOneLine() {
    JRMeasuredText measured = measure(cell(32, 15, 0, 0), LONG_VALUE);

    assertEquals(LONG_VALUE.length(), measured.getTextOffset());
    assertNull(measured.getLineBreakOffsets());
    assertNull(measured.getTextSuffix());
  }

  @Test
  public void shouldReportHeightWithinElementPadding() {
    JRMeasuredText measured = measure(cell(32, 15, 3, 3), LONG_VALUE);

    assertEquals(9f, measured.getTextHeight(), 0f);
  }

  @Test
  public void shouldNotReportNegativeHeight() {
    JRMeasuredText measured = measure(cell(32, 15, 10, 10), LONG_VALUE);

    assertEquals(0f, measured.getTextHeight(), 0f);
  }

  @Test
  public void shouldMeasureEmptyText() {
    JRMeasuredText measured = measure(cell(32, 15, 0, 0), "");

    assertEquals(0, measured.getTextOffset());
  }

  private JRMeasuredText measure(JRDesignTextField cell, String value) {
    JRTextMeasurer measurer =
        factory.createMeasurer(DefaultJasperReportsContext.getInstance(), cell);
    JRStyledText text = new JRStyledText();
    text.append(value);
    return measurer.measure(text, 0, 0, false, true);
  }

  private static JRDesignTextField cell(int width, int height, int topPadding,
      int bottomPadding) {
    JRDesignTextField cell = new JRDesignTextField();
    cell.setWidth(width);
    cell.setHeight(height);
    cell.getLineBox().setTopPadding(topPadding);
    cell.getLineBox().setBottomPadding(bottomPadding);
    return cell;
  }
}
