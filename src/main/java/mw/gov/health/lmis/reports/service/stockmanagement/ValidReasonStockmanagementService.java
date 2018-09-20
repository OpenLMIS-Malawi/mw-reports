package mw.gov.health.lmis.reports.service.stockmanagement;

import mw.gov.health.lmis.reports.service.referencedata.BaseReferenceDataService;
import org.springframework.stereotype.Service;

@Service
public class ValidReasonStockmanagementService
    extends BaseReferenceDataService<StockCardLineItemReasonDto> {

  @Override
  protected String getUrl() {
    return "/api/stockCardLineItemReasons";
  }

  @Override
  protected Class<StockCardLineItemReasonDto> getResultClass() {
    return StockCardLineItemReasonDto.class;
  }

  @Override
  protected Class<StockCardLineItemReasonDto[]> getArrayResultClass() {
    return StockCardLineItemReasonDto[].class;
  }
}
