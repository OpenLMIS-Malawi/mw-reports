package mw.gov.health.lmis.reports.dto.external;

import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class LotDto {

  private UUID id;
  private String lotCode;
  private boolean active;
  private UUID tradeItemId;
  private LocalDate expirationDate;
  private LocalDate manufactureDate;
}
