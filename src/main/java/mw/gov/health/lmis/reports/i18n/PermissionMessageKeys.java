package mw.gov.health.lmis.reports.i18n;

public class PermissionMessageKeys extends MessageKeys {
  public static String ERROR = join(SERVICE_ERROR, "permission");

  public static String ERROR_NO_PERMISSION = join(ERROR, "no", "permission");
  public static final String PERMISSIONS_MISSING = join(ERROR, PERMISSIONS, MISSING);
}
