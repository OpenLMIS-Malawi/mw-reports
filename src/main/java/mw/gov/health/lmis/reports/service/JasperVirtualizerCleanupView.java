package mw.gov.health.lmis.reports.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import net.sf.jasperreports.engine.JRVirtualizer;
import org.springframework.web.servlet.View;

public class JasperVirtualizerCleanupView implements View {

  private View delegate;
  private JRVirtualizer virtualizer;

  public JasperVirtualizerCleanupView(View delegate, JRVirtualizer virtualizer) {
    this.delegate = delegate;
    this.virtualizer = virtualizer;
  }

  @Override
  public String getContentType() {
    return delegate.getContentType();
  }

  @Override
  public void render(Map<String, ?> map, HttpServletRequest httpServletRequest,
                     HttpServletResponse httpServletResponse) throws Exception {
    try {
      delegate.render(map, httpServletRequest, httpServletResponse);
    } finally {
      virtualizer.cleanup();
    }
  }

}