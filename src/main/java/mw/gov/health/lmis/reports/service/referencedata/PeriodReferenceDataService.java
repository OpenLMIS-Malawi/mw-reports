package mw.gov.health.lmis.reports.service.referencedata;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import mw.gov.health.lmis.reports.dto.external.ProcessingPeriodDto;
import mw.gov.health.lmis.utils.RequestParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

  @Value("${time.zoneId}")
  private String timeZoneId;

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

    return getPage("", parameters).getContent();
  }

  /**
   * Retrieves non-future periods.
   *
   * @return A list of periods.
   */
  public List<ProcessingPeriodDto> getNonFuturePeriods() {
    String lastDayOfMonth = LocalDate
        .now(ZoneId.of(timeZoneId))
        .with(TemporalAdjusters.lastDayOfMonth())
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    RequestParameters parameters = RequestParameters
        .init()
        .set("endDate", lastDayOfMonth)
        .set("sort", "startDate,desc");

    return getPage("", parameters).getContent();
  }
}
