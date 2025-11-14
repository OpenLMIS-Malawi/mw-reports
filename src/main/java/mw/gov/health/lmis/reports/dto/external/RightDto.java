package mw.gov.health.lmis.reports.dto.external;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RightDto {
  private UUID id;
  private String name;
  private RightType type;
  private String description;
}
