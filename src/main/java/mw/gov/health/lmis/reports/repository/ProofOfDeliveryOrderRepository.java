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

package mw.gov.health.lmis.reports.repository;

import mw.gov.health.lmis.reports.dto.ProofOfDeliveryOrderDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Reads the order of a proof of delivery from the fulfillment tables.
 */
@Repository
public class ProofOfDeliveryOrderRepository {

  private static final String FIND_BY_PROOF_OF_DELIVERY_ID =
      "SELECT o.programid, o.receivingfacilityid, o.supplyingfacilityid"
          + " FROM fulfillment.proofs_of_delivery pod"
          + " JOIN fulfillment.shipments s ON s.id = pod.shipmentid"
          + " JOIN fulfillment.orders o ON o.id = s.orderid"
          + " WHERE pod.id = ?";

  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * Finds the program and facilities of the order the given proof of delivery belongs to.
   *
   * @param proofOfDeliveryId proof of delivery id.
   * @return the order's program and facilities, empty if the proof of delivery doesn't exist.
   */
  public Optional<ProofOfDeliveryOrderDto> findByProofOfDeliveryId(UUID proofOfDeliveryId) {
    List<ProofOfDeliveryOrderDto> orders = jdbcTemplate.query(FIND_BY_PROOF_OF_DELIVERY_ID,
        (rs, rowNum) -> new ProofOfDeliveryOrderDto(
            rs.getObject("programid", UUID.class),
            rs.getObject("receivingfacilityid", UUID.class),
            rs.getObject("supplyingfacilityid", UUID.class)),
        proofOfDeliveryId);

    return orders.stream().findFirst();
  }
}
