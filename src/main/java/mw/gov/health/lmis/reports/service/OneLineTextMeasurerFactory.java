package mw.gov.health.lmis.reports.service;

import net.sf.jasperreports.engine.JRCommonText;
import net.sf.jasperreports.engine.JRLineBox;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.fill.JRMeasuredText;
import net.sf.jasperreports.engine.fill.JRTextMeasurer;
import net.sf.jasperreports.engine.util.JRTextMeasurerFactory;

/**
 * Reports every text as fitting its element on one line, without measuring it. Exporters that
 * write the full text whatever the layout, such as CSV, never read where the text would be cut,
 * and finding that cut dominates the fill of large reports with narrow cells.
 */
public class OneLineTextMeasurerFactory implements JRTextMeasurerFactory {

  @Override
  public JRTextMeasurer createMeasurer(JasperReportsContext context, JRCommonText element) {
    JRLineBox box = element.getLineBox();
    float width = element.getWidth();
    float height = Math.max(0,
        element.getHeight() - box.getTopPadding() - box.getBottomPadding());
    return (text, remainingTextStart, availableStretchHeight, indentFirstLine, canOverflow) ->
        new OneLine(text.length(), width, height);
  }

  private static final class OneLine implements JRMeasuredText {
    private final int textEnd;
    private final float width;
    private final float height;

    private OneLine(int textEnd, float width, float height) {
      this.textEnd = textEnd;
      this.width = width;
      this.height = height;
    }

    @Override
    public float getLeadingOffset() {
      return 0;
    }

    @Override
    public float getLineSpacingFactor() {
      return 1;
    }

    @Override
    public float getTextWidth() {
      return width;
    }

    @Override
    public float getTextHeight() {
      return height;
    }

    @Override
    public int getTextOffset() {
      return textEnd;
    }

    @Override
    public boolean isLeftToRight() {
      return true;
    }

    @Override
    public boolean isParagraphCut() {
      return false;
    }

    @Override
    public String getTextSuffix() {
      return null;
    }

    @Override
    public short[] getLineBreakOffsets() {
      return null;
    }
  }
}
