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

import mw.gov.health.lmis.reports.dto.external.StockCardDto;
import mw.gov.health.lmis.utils.RequestParameters;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
public class StockCardReferenceDataService
    extends BaseReferenceDataService<StockCardDto> {

  @Override
  protected String getUrl() {
    return "/api/stockCards";
  }

  @Override
  protected Class<StockCardDto> getResultClass() {
    return StockCardDto.class;
  }

  @Override
  protected Class<StockCardDto[]> getArrayResultClass() {
    return StockCardDto[].class;
  }

  /**
   * Finds stock cards by their ids.
   *
   * @param ids ids to look for.
   * @return list of stock cards.
   */
  public List<StockCardDto> findByIds(Collection<UUID> ids) {
    return getPage("", RequestParameters
        .init()
        .set("id", ids)
        .setPage(new PageRequest(0, Integer.MAX_VALUE, Sort.Direction.ASC, "id")))
        .getContent();
  }
}
