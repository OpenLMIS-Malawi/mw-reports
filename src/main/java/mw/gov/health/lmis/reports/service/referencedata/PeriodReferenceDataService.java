package mw.gov.health.lmis.reports.service.referencedata;

import java.util.List;
import mw.gov.health.lmis.reports.dto.external.ProcessingPeriodDto;
import mw.gov.health.lmis.utils.RequestParameters;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

@Service
public class PeriodReferenceDataService extends BaseReferenceDataService<ProcessingPeriodDto> {

  @Override
  protected String getUrl() {
    return "/api/processingPeriods/";
  }

  @Override
  protected Class<ProcessingPeriodDto> getResultClass() {
    return ProcessingPeriodDto.class;
  }

  @Override
  protected Class<ProcessingPeriodDto[]> getArrayResultClass() {
    return ProcessingPeriodDto[].class;
  }

  /**
   * Retrieves periods from the reference data service by schedule ID and start date.
   *
   * @param processingScheduleId UUID of the schedule
   * @param startDate            the start date (only include periods past this date)
   * @return A list of periods matching search criteria
   */
  public Collection<ProcessingPeriodDto> search(UUID processingScheduleId, LocalDate startDate) {
    RequestParameters parameters = RequestParameters
        .init()
        .set("processingScheduleId", processingScheduleId)
        .set("startDate", startDate);

    return findAll("searchByScheduleAndDate", parameters);
  }

  /**
   * Retrieves all periods.
   *
   * @return A list of periods
   */
  public List<ProcessingPeriodDto> findAll() {
    return getPage("", RequestParameters.init()).getContent();
  }

  /**
   * Retrieves periods from the reference data service by program ID and facility ID.
   *
   * @param programId  UUID of the program
   * @param facilityId UUID of the facility
   * @return A list of periods matching search criteria
   */
  public Collection<ProcessingPeriodDto> searchByProgramAndFacility(UUID programId,
                                                                    UUID facilityId) {
    RequestParameters parameters = RequestParameters
        .init()
        .set("programId", programId)
        .set("facilityId", facilityId);

    return findAll("search", parameters);
  }
}
