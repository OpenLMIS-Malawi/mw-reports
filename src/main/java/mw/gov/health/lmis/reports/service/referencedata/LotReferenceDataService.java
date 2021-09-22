package mw.gov.health.lmis.reports.service.referencedata;

import mw.gov.health.lmis.reports.dto.external.LotDto;
import mw.gov.health.lmis.utils.RequestParameters;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LotReferenceDataService extends BaseReferenceDataService<LotDto> {
  @Override
  protected String getUrl() {
    return "/api/lots";
  }

  @Override
  protected Class<LotDto> getResultClass() {
    return LotDto.class;
  }

  @Override
  protected Class<LotDto[]> getArrayResultClass() {
    return LotDto[].class;
  }

  public LotDto findById(UUID id) {
    return findOne("/" + id, RequestParameters.init());
  }
}
