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

package mw.gov.health.lmis.reports.service.fulfillment;

import mw.gov.health.lmis.reports.dto.external.ProofOfDeliveryDto;
import mw.gov.health.lmis.utils.RequestParameters;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProofOfDeliveryDataService extends BaseFulfillmentService<ProofOfDeliveryDto> {
  @Override
  protected String getUrl() {
    return "/api/proofsOfDelivery";
  }

  @Override
  protected Class<ProofOfDeliveryDto> getResultClass() {
    return ProofOfDeliveryDto.class;
  }

  @Override
  protected Class<ProofOfDeliveryDto[]> getArrayResultClass() {
    return ProofOfDeliveryDto[].class;
  }

  /**
   * Finds proof of delivery matching all of the provided parameters.
   */
  public ProofOfDeliveryDto findProofOfDelivery(UUID id) {

    return findOne("/" + id, RequestParameters.init());
  }
}
