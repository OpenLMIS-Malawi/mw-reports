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

package mw.gov.health.lmis.reports.service.referencedata;

import mw.gov.health.lmis.reports.dto.external.StockCardSummaryDto;
import mw.gov.health.lmis.utils.RequestParameters;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StockCardSummariesReferenceDataService
    extends BaseReferenceDataService<StockCardSummaryDto> {

  @Override
  protected String getUrl() {
    return "/api/v2/stockCardSummaries/";
  }

  @Override
  protected Class<StockCardSummaryDto> getResultClass() {
    return StockCardSummaryDto.class;
  }

  @Override
  protected Class<StockCardSummaryDto[]> getArrayResultClass() {
    return StockCardSummaryDto[].class;
  }

  /**
   * Finds stock card summaries by programId and facilityId.
   *
   * @param program id of program.
   * @param facility id of facility.
   * @return list of stock card summaries.
   */
  public List<StockCardSummaryDto> findStockCardsSummaries(UUID program, UUID facility) {
    return getPage("", RequestParameters
        .init()
        .set("programId", program)
        .set("facilityId", facility)
        .set("nonEmptyOnly", "true"))
        .getContent();
  }
}
