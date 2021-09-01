package mw.gov.health.lmis.reports.i18n;

import java.util.Arrays;

public class MessageKeys {
  private static final String DELIMITER = ".";

  // General
  protected static final String SERVICE = "malawi.reports";
  protected static final String SERVICE_ERROR = join(SERVICE, "error");
  protected static final String REQUISITION_ERROR = "requisition.error";

  public static final String ERROR_IO = REQUISITION_ERROR + ".io";
  public static final String ERROR_JASPER_FILE_FORMAT = REQUISITION_ERROR + ".jasper.file.format";
  public static final String STATUS_CHANGE_USER_SYSTEM =
          REQUISITION_ERROR + ".statusChange.user.system";
  public static final String ERROR_REQUISITION_NOT_FOUND = REQUISITION_ERROR
          + ".requisitionNotFound";
  private static final String ERROR_PREFIX = SERVICE + ".error";

  protected static final String NOT_FOUND = "notFound";

  //report
  public static final String ERROR_GENERATE_REPORT_FAILED = ERROR_PREFIX + ".generateReport.failed";
  public static final String ERROR_REPORTING_TEMPLATE_NOT_FOUND_WITH_NAME = ERROR_PREFIX
      + ".reporting.template.notFound.with.name";
  public static final String ERROR_CLASS_NOT_FOUND = ERROR_PREFIX + ".classNotFound";

  protected static String join(String... params) {
    return String.join(DELIMITER, Arrays.asList(params));
  }

  protected MessageKeys() {
    throw new UnsupportedOperationException();
  }
}
