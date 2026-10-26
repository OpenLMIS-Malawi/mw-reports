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

import static mw.gov.health.lmis.reports.service.ViewPermissionService.PODS_MANAGE;
import static mw.gov.health.lmis.reports.service.ViewPermissionService.PODS_VIEW;
import static mw.gov.health.lmis.reports.service.ViewPermissionService.SHIPMENTS_EDIT;
import static mw.gov.health.lmis.reports.service.ViewPermissionService.STOCK_CARDS_VIEW;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.UUID;
import mw.gov.health.lmis.reports.dto.ProofOfDeliveryOrderDto;
import mw.gov.health.lmis.reports.exception.PermissionMessageException;
import mw.gov.health.lmis.utils.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ViewPermissionServiceTest {

  @Mock
  private PermissionService permissionService;

  @InjectMocks
  private ViewPermissionService viewPermissionService;

  private final UUID programId = UUID.randomUUID();
  private final UUID receivingFacilityId = UUID.randomUUID();
  private final UUID supplyingFacilityId = UUID.randomUUID();
  private final ProofOfDeliveryOrderDto proofOfDelivery =
      new ProofOfDeliveryOrderDto(programId, receivingFacilityId, supplyingFacilityId);

  @Test(expected = PermissionMessageException.class)
  public void shouldRejectStockCardPrintWithoutStockCardsView() {
    doThrow(new PermissionMessageException(new Message("key"))).when(permissionService)
        .checkPermission(STOCK_CARDS_VIEW, programId, receivingFacilityId, null);

    viewPermissionService.canViewStockCard(programId, receivingFacilityId);
  }

  @Test
  public void shouldAllowProofOfDeliveryWithPodsView() {
    when(permissionService.hasPermission(PODS_VIEW, receivingFacilityId, programId))
        .thenReturn(true);

    viewPermissionService.canViewProofOfDelivery(proofOfDelivery);
  }

  @Test
  public void shouldAllowProofOfDeliveryWithPodsManage() {
    when(permissionService.hasPermission(PODS_MANAGE, receivingFacilityId, programId))
        .thenReturn(true);

    viewPermissionService.canViewProofOfDelivery(proofOfDelivery);
  }

  @Test
  public void shouldAllowProofOfDeliveryWithShipmentsEditForSupplyingFacility() {
    when(permissionService.hasPermission(SHIPMENTS_EDIT, supplyingFacilityId)).thenReturn(true);

    viewPermissionService.canViewProofOfDelivery(proofOfDelivery);
  }

  @Test(expected = PermissionMessageException.class)
  public void shouldRejectProofOfDeliveryWithoutPodOrShipmentRights() {
    viewPermissionService.canViewProofOfDelivery(proofOfDelivery);
  }

  @Test(expected = PermissionMessageException.class)
  public void shouldRejectProofOfDeliveryWithPodsViewForAnotherFacility() {
    when(permissionService.hasPermission(PODS_VIEW, supplyingFacilityId, programId))
        .thenReturn(true);

    viewPermissionService.canViewProofOfDelivery(proofOfDelivery);
  }
}
