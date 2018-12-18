package mw.gov.health.lmis.reports.dto.external;


import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {

  public static final Integer DISTRICT_LEVEL = 3;
  public static final Integer REGION_LEVEL = 2;

  @Getter
  @Setter
  private UUID id;

  @Getter
  @Setter
  private UUID externalId;

  @Getter
  @Setter
  private Boolean emergency;

  @Getter
  @Setter
  private FacilityDto facility;

  @Getter
  @Setter
  private ProcessingPeriodDto processingPeriod;

  @Getter
  @Setter
  private ZonedDateTime createdDate;

  @Getter
  @Setter
  private UserDto createdBy;

  @Getter
  @Setter
  private ProgramDto program;

  @Getter
  @Setter
  private FacilityDto requestingFacility;

  @Getter
  @Setter
  private FacilityDto receivingFacility;

  @Getter
  @Setter
  private FacilityDto supplyingFacility;

  @Getter
  @Setter
  private String orderCode;

  @Getter
  @Setter
  private OrderStatusDto status;

  @Getter
  @Setter
  private BigDecimal quotedCost;

  @Getter
  @Setter
  private List<OrderLineItemDto> orderLineItems;

  @Getter
  @Setter
  private List<StatusMessageDto> statusMessages;

  @Getter
  @Setter
  private List<StatusChangeDto> statusChanges;

  /**
   * Get status change with given status.
   * @return status change
   */
  @JsonIgnore
  public StatusChangeDto getStatusChangeByStatus(RequisitionStatusDto status) {
    return Optional.of(statusChanges).orElse(new ArrayList<>()).stream()
            .filter(statusChange -> status.equals(statusChange.getStatus())
    ).findFirst().orElse(null);
  }

  /**
   * Get zone of the facility that has the district level.
   * @return district of the facility.
   */
  @JsonIgnore
  public GeographicZoneDto getThirdLevelFacility() {
    return getFacility().getZoneByLevelNumber(DISTRICT_LEVEL);
  }

  /**
   * Get zone of the facility that has the region level.
   * @return region of the facility.
   */
  @JsonIgnore
  public GeographicZoneDto getSecondLevelFacility() {
    return getFacility().getZoneByLevelNumber(REGION_LEVEL);
  }

  /**
   * Get status change that is AUTHORIZED.
   * @return authorized status change.
   */
  @JsonIgnore
  public StatusChangeDto getAuthorizedStatusChange() {
    return Optional.ofNullable(getStatusChangeByStatus(RequisitionStatusDto.AUTHORIZED))
            .orElse(new StatusChangeDto());
  }

  /**
   * Get status change that is APPROVED.
   * @return approved status change.
   */
  @JsonIgnore
  public StatusChangeDto getApprovedStatusChange() {
    return Optional.ofNullable(getStatusChangeByStatus(RequisitionStatusDto.APPROVED))
            .orElse(new StatusChangeDto());
  }

  /**
   * Get status changes that are APPROVED.
   * @return approved status change.
   */
  @JsonIgnore
  public Set<StatusChangeDto> getApprovedStatusChanges() {
    return Optional.of(statusChanges).orElse(new ArrayList<>()).stream()
        .filter(statusChange -> RequisitionStatusDto.APPROVED.equals(statusChange.getStatus()))
        .sorted()
        .collect(Collectors.toSet());
  }

  /**
   * Get status change that is RELEASED.
   * @return released status change.
   */
  @JsonIgnore
  public StatusChangeDto getReleasedStatusChange() {
    return Optional.ofNullable(getStatusChangeByStatus(RequisitionStatusDto.RELEASED))
            .orElse(new StatusChangeDto());
  }
}
