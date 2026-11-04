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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StockCardReferenceDataService
    extends BaseReferenceDataService<StockCardDto> {

  // Each id adds about 40 characters to the request URL; 150 ids keep the request line well
  // under nginx's default 8 KB limit.
  static final int MAX_IDS_PER_REQUEST = 150;

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
   * Finds stock cards by their ids, requesting them in batches of {@link #MAX_IDS_PER_REQUEST}.
   * Stockmanagement returns every stock card when no id is given, so an empty collection is
   * answered without calling it. Null and duplicate ids are skipped.
   *
   * @param ids ids to look for.
   * @return list of stock cards, empty when no ids are given.
   */
  public List<StockCardDto> findByIds(Collection<UUID> ids) {
    if (ids == null) {
      return Collections.emptyList();
    }

    List<UUID> idList = ids.stream()
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.toList());
    List<StockCardDto> stockCards = new ArrayList<>(idList.size());

    for (int from = 0; from < idList.size(); from += MAX_IDS_PER_REQUEST) {
      List<UUID> batch = idList.subList(from,
          Math.min(from + MAX_IDS_PER_REQUEST, idList.size()));

      stockCards.addAll(getPage("", RequestParameters
          .init()
          .set("id", batch)
          .setPage(new PageRequest(0, batch.size(), Sort.Direction.ASC, "id")))
          .getContent());
    }

    return stockCards;
  }
}
