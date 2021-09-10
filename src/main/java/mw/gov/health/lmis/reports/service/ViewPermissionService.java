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

package mw.gov.health.lmis.reports.service;

import mw.gov.health.lmis.reports.dto.external.ProofOfDeliveryDto;
import mw.gov.health.lmis.reports.dto.external.RequisitionDto;
import mw.gov.health.lmis.reports.exception.MissingPermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

@Service
public class ViewPermissionService {

  @Autowired
  private PermissionService permissionService;

  public static final String ORDERS_VIEW = "ORDERS_VIEW";
  public static final String REPORTS_VIEW = "REPORTS_VIEW";
  public static final String REQUISITION_VIEW = "REQUISITION_VIEW";
  public static final String STOCK_CARDS_VIEW = "STOCK_CARDS_VIEW";
  public static final String PODS_MANAGE = "PODS_MANAGE";
  public static final String PODS_VIEW = "PODS_VIEW";
  public static final String SHIPMENTS_EDIT = "SHIPMENTS_EDIT";

  public static final UUID ORDER_ID =
      UUID.fromString("3c9d1e80-1e45-4adb-97d9-208b6fdceeec");
  public static final UUID AGGREGATE_ORDERS_ID =
      UUID.fromString("f28d0ebd-7276-4453-bc3c-48556a4bd25a");

  /**
   * Checks if current user has permission to view a requisition.
   */
  public void canViewRequisition(RequisitionDto requisition) {
    permissionService.checkPermission(REQUISITION_VIEW, requisition.getProgram().getId(),
        requisition.getFacility().getId(), null);
  }

  /**
   * Checks if current user has permission to view stock card.
   *
   * @param programId  program id.
   * @param facilityId facility id.
   */
  public void canViewStockCard(UUID programId, UUID facilityId) {
    permissionService.hasPermission(STOCK_CARDS_VIEW, programId, facilityId, null);
  }

  /**
   * Check whether the user has REPORTS_VIEW permission.
   * @param templateId (optional) id of the report; if it equals to Aggregate Orders,
   *                   the user can have either the ORDERS_VIEW or REPORTS_VIEW permission
   */
  public void canViewReports(UUID templateId) {
    if (templateId != null && (templateId.equals(AGGREGATE_ORDERS_ID)
        || templateId.equals(ORDER_ID))) {
      canViewReportsOrOrders();
    } else {
      permissionService.checkPermission(REPORTS_VIEW);
    }
  }

  public void canViewReportsOrOrders() {
    permissionService.checkAnyPermission(Arrays.asList(REPORTS_VIEW, ORDERS_VIEW));
  }

  /**
   * Checks if user has permission to view PoD.
   */
  public void canViewPod(ProofOfDeliveryDto proofOfDelivery) throws MissingPermissionException {
    UUID receivingFacilityId = proofOfDelivery.getReceivingFacilityId();
    UUID supplyingFacilityId = proofOfDelivery.getSupplyingFacilityId();
    UUID programId = proofOfDelivery.getProgramId();

    if (permissionService.hasPermission(PODS_MANAGE, receivingFacilityId, programId)
        || permissionService.hasPermission(PODS_VIEW, receivingFacilityId, programId)
        || permissionService.hasPermission(SHIPMENTS_EDIT, supplyingFacilityId)) {
      return;
    }

    throw new MissingPermissionException(PODS_MANAGE, PODS_VIEW, SHIPMENTS_EDIT);
  }


}
