package mw.gov.health.lmis.reports.exception;

import mw.gov.health.lmis.utils.Message;

public class JasperReportViewException extends BaseLocalizedException {

  public JasperReportViewException(Message message, Throwable throwable) {
    super(throwable, message.toString());
  }

  public JasperReportViewException(Throwable cause, String messageKey, String... params) {
    super(cause, messageKey, params);
  }
}
