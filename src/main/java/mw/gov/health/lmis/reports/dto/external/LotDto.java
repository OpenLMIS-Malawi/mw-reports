package mw.gov.health.lmis.reports.dto.external;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class LotDto {
  private UUID id;
  private String lotCode;
  private boolean active;
  private UUID tradeItemId;
  @JsonFormat(shape = STRING)
  private LocalDate expirationDate;
  @JsonFormat(shape = STRING)
  private LocalDate manufactureDate;
}
