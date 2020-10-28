package mw.gov.health.lmis.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mw.gov.health.lmis.reports.dto.external.RequisitionDto;
import mw.gov.health.lmis.reports.dto.external.RequisitionLineItemDto;
import mw.gov.health.lmis.reports.dto.external.UserDto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequisitionReportDto {
  private RequisitionDto requisition;
  private List<RequisitionLineItemDto> fullSupply;
  private List<RequisitionLineItemDto> nonFullSupply;
  private BigDecimal fullSupplyTotalCost;
  private BigDecimal nonFullSupplyTotalCost;
  private BigDecimal totalCost;
  private UserDto initiatedBy;
  private ZonedDateTime initiatedDate;
  private UserDto submittedBy;
  private ZonedDateTime submittedDate;
  private UserDto authorizedBy;
  private ZonedDateTime authorizedDate;
  private Boolean chamFacility;
}
