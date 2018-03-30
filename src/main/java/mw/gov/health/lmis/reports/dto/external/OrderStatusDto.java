package mw.gov.health.lmis.reports.dto.external;

public enum OrderStatusDto {
  ORDERED,
  FULFILLING,
  SHIPPED,
  RECEIVED,
  TRANSFER_FAILED,
  IN_ROUTE,
  READY_TO_PACK;
}
