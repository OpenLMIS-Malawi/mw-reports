package mw.gov.health.lmis.testutils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import mw.gov.health.lmis.reports.service.stockmanagement.ReasonCategory;
import mw.gov.health.lmis.reports.service.stockmanagement.ReasonType;
import mw.gov.health.lmis.reports.service.stockmanagement.StockCardLineItemReasonDto;

public class StockCardLineItemReasonDtoDataBuilder {

  private static int instanceNumber = 0;

  private UUID id;
  private String name;
  private String description;
  private ReasonType reasonType;
  private ReasonCategory reasonCategory;
  private Boolean isFreeTextAllowed;
  private List<String> tags;

  /**
   * Creates instance to be used for building {@link StockCardLineItemReasonDto}.
   */
  public StockCardLineItemReasonDtoDataBuilder() {
    instanceNumber++;

    id = UUID.randomUUID();
    name = "Stock " + instanceNumber;
    description = "Stock Line Item Reason " + instanceNumber;
    reasonType = ReasonType.CREDIT;
    reasonCategory = ReasonCategory.ADJUSTMENT;
    isFreeTextAllowed = true;
    tags = new ArrayList<>();
  }

  public StockCardLineItemReasonDto build() {
    return new StockCardLineItemReasonDto(id, name, description, reasonType, reasonCategory,
        isFreeTextAllowed, tags);
  }

  public StockCardLineItemReasonDtoDataBuilder withId(UUID id) {
    this.id = id;
    return this;
  }
}
