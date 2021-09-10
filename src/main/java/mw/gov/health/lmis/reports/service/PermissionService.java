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

import static mw.gov.health.lmis.reports.i18n.PermissionMessageKeys.ERROR_NO_PERMISSION;

import mw.gov.health.lmis.reports.dto.external.DetailedRoleAssignmentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import mw.gov.health.lmis.reports.dto.external.ResultDto;
import mw.gov.health.lmis.reports.dto.external.RightDto;
import mw.gov.health.lmis.reports.dto.external.UserDto;
import mw.gov.health.lmis.reports.exception.PermissionMessageException;
import mw.gov.health.lmis.reports.service.referencedata.UserReferenceDataService;
import mw.gov.health.lmis.utils.AuthenticationHelper;
import mw.gov.health.lmis.utils.Message;

import java.util.List;
import java.util.UUID;

@Service
public class PermissionService {
  public static final String REPORT_TEMPLATES_EDIT = "REPORT_TEMPLATES_EDIT";
  public static final String STOCK_INVENTORIES_EDIT = "STOCK_INVENTORIES_EDIT";

  public static final UUID AGGREGATE_ORDERS_ID =
          UUID.fromString("f28d0ebd-7276-4453-bc3c-48556a4bd25a");
  public static final UUID AGGREGATE_ORDERS_XLS_ID =
      UUID.fromString("5e378334-d1fe-4915-902e-22ecd0a61f5b");
  public static final String ORDERS_VIEW = "ORDERS_VIEW";


  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  public void canEditReportTemplates() {
    checkPermission(REPORT_TEMPLATES_EDIT);
  }

  /**
   * Checks if current user has permission to do physical inventory.
   *
   * @param programId  program id.
   * @param facilityId facility id.
   */
  public void canEditPhysicalInventory(UUID programId, UUID facilityId) {
    hasPermission(STOCK_INVENTORIES_EDIT, programId, facilityId, null);
  }



  /**
   * Checks if current user has permission.
   *
   * @param rightName  right name.
   */
  public void checkPermission(String rightName) {
    if (!hasPermission(rightName)) {
      throw new PermissionMessageException(new Message(ERROR_NO_PERMISSION, rightName));
    }
  }

  /**
   * Checks if current user has permission.
   *
   * @param rightName  right name.
   * @param program program id.
   * @param facility facility id.
   * @param warehouse warehouse id.
   */
  public void checkPermission(String rightName, UUID program, UUID facility, UUID warehouse) {
    if (!hasPermission(rightName, program, facility, warehouse)) {
      throw new PermissionMessageException(new Message(ERROR_NO_PERMISSION, rightName));
    }
  }

  /**
   * Checks if current user has any permission.
   *
   * @param rightNames  list of rights names.
   */
  public void checkAnyPermission(List<String> rightNames) {
    if (rightNames.stream().noneMatch(this::hasPermission)) {
      throw new PermissionMessageException(new Message(ERROR_NO_PERMISSION, rightNames));
    }
  }

  private Boolean hasPermission(String rightName) {
    if (ORDERS_VIEW.equals(rightName)) {
      return hasFulfillmentPermission(rightName);
    }
    UserDto user = authenticationHelper.getCurrentUser();
    RightDto right = authenticationHelper.getRight(rightName);
    ResultDto<Boolean> result = userReferenceDataService.hasRight(user.getId(), right.getId());
    return null != result && result.getResult();
  }

  /**
   * Checks if current user has permission to do physical inventory.
   *
   * @param rightName  right name.
   * @param program program id.
   * @param facility facility id.
   * @param warehouse warehouse id.
   * @return boolean
   */
  public Boolean hasPermission(String rightName, UUID program, UUID facility, UUID warehouse) {
    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext()
            .getAuthentication();
    if (authentication.isClientOnly()) {
      return true;
    }
    UserDto user = authenticationHelper.getCurrentUser();
    RightDto right = authenticationHelper.getRight(rightName);
    ResultDto<Boolean> result = userReferenceDataService.hasRight(
            user.getId(), right.getId(), program, facility, warehouse
    );
    return null != result && result.getResult();
  }

  public boolean hasPermission(String rightName, UUID facility, UUID program) {
    return hasPermission(rightName, program, facility, null);
  }

  public boolean hasPermission(String rightName, UUID warehouse) {
    return hasPermission(rightName, null, null, warehouse);
  }

  // Check if a user has fulfillment permission without specifying the warehouse
  private Boolean hasFulfillmentPermission(String rightName) {
    UserDto user = authenticationHelper.getCurrentUser();
    List<DetailedRoleAssignmentDto> roleAssignments =
            userReferenceDataService.getUserRightsAndRoles(user.getId());

    return roleAssignments.stream().anyMatch(
        assignment -> assignment.getRole().getRights().stream().anyMatch(
            right -> right.getName().equals(rightName)
    ));
  }
}
