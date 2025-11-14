package mw.gov.health.lmis.reports.dto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mw.gov.health.lmis.reports.domain.DashboardReport;
import mw.gov.health.lmis.reports.domain.DashboardReport.Exporter;
import mw.gov.health.lmis.reports.domain.DashboardReport.Importer;
import mw.gov.health.lmis.reports.domain.ReportCategory;

import mw.gov.health.lmis.utils.ReportType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardReportDto implements Importer, Exporter {
  private UUID id;
  private String name;
  private String url;
  private ReportType type;
  private boolean enabled;
  private boolean showOnHomePage;
  private ReportCategory category;
  private String rightName;

  /**
   * Create new instance of DashboardReportDto based on given {@link DashboardReport}.
   *
   * @param dashboardReport instance of Dashboard Report.
   * @return new instance of DashboardReportDto.
   */
  public static DashboardReportDto newInstance(DashboardReport dashboardReport) {
    if (dashboardReport == null) {
      return null;
    }
    DashboardReportDto dashboardReportDto = new DashboardReportDto();
    dashboardReport.export(dashboardReportDto);
    return dashboardReportDto;
  }

  /**
   * Create new list of DashboardReportDto based on given list of {@link DashboardReport}.
   *
   * @param dashboardReports list of {@link DashboardReport}.
   * @return new list of DashboardReportDto.
   */
  public static List<DashboardReportDto> newInstance(Iterable<DashboardReport> dashboardReports) {
    return StreamSupport.stream(dashboardReports.spliterator(), false)
        .map(DashboardReportDto::newInstance)
        .collect(Collectors.toList());
  }
}