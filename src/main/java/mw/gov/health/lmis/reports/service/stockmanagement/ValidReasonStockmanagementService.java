package mw.gov.health.lmis.reports.service.stockmanagement;

import mw.gov.health.lmis.reports.service.referencedata.BaseReferenceDataService;
import org.springframework.stereotype.Service;

@Service
public class ValidReasonStockmanagementService
    extends BaseReferenceDataService<ValidReasonAssignmentDto> {

  @Override
  protected String getUrl() {
    return "/api/validReasons";
  }

  @Override
  protected Class<ValidReasonAssignmentDto> getResultClass() {
    return ValidReasonAssignmentDto.class;
  }

  @Override
  protected Class<ValidReasonAssignmentDto[]> getArrayResultClass() {
    return ValidReasonAssignmentDto[].class;
  }
}
