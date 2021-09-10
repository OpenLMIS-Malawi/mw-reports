/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package mw.gov.health.lmis.reports.dto.external;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.UUID;

@ToString
@Getter
@NoArgsConstructor
@JsonDeserialize(builder = StockCardDto.Builder.class)
public class StockCardDto {

  private UUID id;
  private Integer stockOnHand;
  private FacilityDto facility;
  private ProgramDto program;
  private OrderableDto orderable;
  private LotDto lot;

  /**
   * Constructor of stock card.
   */
  public StockCardDto(UUID id,
                      Integer stockOnHand,
                      FacilityDto facility,
                      ProgramDto program,
                      OrderableDto orderable,
                      LotDto lot) {
    this.id = id;
    this.stockOnHand = stockOnHand;
    this.facility = facility;
    this.program = program;
    this.orderable = orderable;
    this.lot = lot;
  }

  @JsonPOJOBuilder
  public static class Builder {
    UUID id;
    Integer stockOnHand;
    FacilityDto facility;
    ProgramDto program;
    OrderableDto orderable;
    LotDto lot;

    public StockCardDto.Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    public StockCardDto.Builder withStockOnHand(Integer stockOnHand) {
      this.stockOnHand = stockOnHand;
      return this;
    }

    public StockCardDto.Builder withFacility(FacilityDto facility) {
      this.facility = facility;
      return this;
    }

    public StockCardDto.Builder withProgram(ProgramDto program) {
      this.program = program;
      return this;
    }

    public StockCardDto.Builder withOrderable(OrderableDto orderable) {
      this.orderable = orderable;
      return this;
    }

    public StockCardDto.Builder withLot(LotDto lot) {
      this.lot = lot;
      return this;
    }

    /**
     * Build method of stock card builder.
     * @return {@link StockCardDto}
     */
    public StockCardDto build() {
      return new StockCardDto(
          id,
          stockOnHand,
          facility,
          program,
          orderable,
          lot);
    }
  }
}
