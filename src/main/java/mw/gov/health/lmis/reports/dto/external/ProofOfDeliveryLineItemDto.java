package mw.gov.health.lmis.reports.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import mw.gov.health.lmis.reports.service.referencedata.ObjectReferenceDto;

import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProofOfDeliveryLineItemDto {
  private UUID id;
  private String serviceUrl;
  private ObjectReferenceDto orderable;
  private ObjectReferenceDto lot;
  private Integer quantityAccepted;
  private Boolean useVvm;
  private String vvmStatus;
  private Integer quantityRejected;
  private UUID rejectionReasonId;
  private String notes;
}
