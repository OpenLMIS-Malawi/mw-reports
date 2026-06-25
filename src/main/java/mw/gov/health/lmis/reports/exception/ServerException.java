package mw.gov.health.lmis.reports.exception;

import mw.gov.health.lmis.utils.Message;

/**
 * Exception representing a server-side failure that is safe to surface to the client
 * as a localized message but should map to an HTTP 500 status.
 */
public class ServerException extends BaseMessageException {

  public ServerException(Message message) {
    super(message);
  }

  public ServerException(Message message, Throwable cause) {
    super(message, cause);
  }
}
