package mw.gov.health.lmis.testutils;

import java.util.UUID;
import mw.gov.health.lmis.reports.service.referencedata.ObjectReferenceDto;
import mw.gov.health.lmis.reports.service.stockmanagement.StockCardLineItemReasonDto;
import mw.gov.health.lmis.reports.service.stockmanagement.ValidReasonAssignmentDto;

public class ValidReasonAssignmentDtoDataBuilder {
  private UUID id;
  private ObjectReferenceDto program;
  private ObjectReferenceDto facilityType;
  private Boolean hidden;
  private StockCardLineItemReasonDto reason;

  /**
   * Creates instance to be used for building {@link ValidReasonAssignmentDto}.
   */
  public ValidReasonAssignmentDtoDataBuilder() {
    id = UUID.randomUUID();
    program = new ObjectReferenceDto("olmis.org", "api/programs", UUID.randomUUID());
    facilityType = new ObjectReferenceDto("olmis.org", "api/facilityTypes", UUID.randomUUID());
    hidden = false;
    reason = new StockCardLineItemReasonDtoDataBuilder().build();
  }

  public ValidReasonAssignmentDto build() {
    return new ValidReasonAssignmentDto(id, program, facilityType, hidden, reason);
  }

  public ValidReasonAssignmentDtoDataBuilder withHidden(Boolean hidden) {
    this.hidden = hidden;
    return this;
  }
}
