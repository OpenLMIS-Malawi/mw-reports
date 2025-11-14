package mw.gov.health.lmis.reports.exception;

import mw.gov.health.lmis.utils.Message;

/**
 * Exception thrown when resource was not found.
 */
public class NotFoundMessageException extends BaseMessageException {
  public NotFoundMessageException(String message) {
    super(message);
  }

  public NotFoundMessageException(Message message) {
    super(message);
  }
}
