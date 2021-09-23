package mw.gov.health.lmis.reports.i18n;

import java.util.Arrays;

public class MessageKeys {
  private static final String DELIMITER = ".";

  // General
  protected static final String SERVICE = "malawi.reports";
  protected static final String SERVICE_ERROR = join(SERVICE, "error");
  protected static final String REQUISITION_ERROR = "requisition.error";
  private static final String PROOF_OF_DELIVERY = "proofOfDelivery";

  public static final String ERROR_IO = REQUISITION_ERROR + ".io";
  public static final String ERROR_JASPER_FILE_FORMAT = REQUISITION_ERROR + ".jasper.file.format";
  public static final String STATUS_CHANGE_USER_SYSTEM =
          REQUISITION_ERROR + ".statusChange.user.system";
  public static final String ERROR_REQUISITION_NOT_FOUND = REQUISITION_ERROR
          + ".requisitionNotFound";
  private static final String PHYSICAL_INVENTORY_ERROR_PREFIX = SERVICE_ERROR
          + ".physicalInventory";
  public static final String ERROR_PHYSICAL_INVENTORY_NOT_FOUND =
      PHYSICAL_INVENTORY_ERROR_PREFIX + ".notFound";

  protected static final String NOT_FOUND = "notFound";
  protected static final String PERMISSION = "permission";
  protected static final String PERMISSIONS = PERMISSION + "s";
  protected static final String MISSING = "missing";
  public static final String ERROR_PHYSICAL_INVENTORY_FORMAT_NOT_ALLOWED =
      PHYSICAL_INVENTORY_ERROR_PREFIX + ".format.notAllowed";

  public static final String PROOF_OF_DELIVERY_NOT_FOUND =
      join(SERVICE_ERROR, PROOF_OF_DELIVERY, NOT_FOUND);

  protected static String join(String... params) {
    return String.join(DELIMITER, Arrays.asList(params));
  }

  protected MessageKeys() {
    throw new UnsupportedOperationException();
  }
}
