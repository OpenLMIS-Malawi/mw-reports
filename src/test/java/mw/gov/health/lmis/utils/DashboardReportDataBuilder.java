package mw.gov.health.lmis.utils;

import java.util.UUID;
import org.apache.commons.lang.RandomStringUtils;
import mw.gov.health.lmis.reports.domain.DashboardReport;
import mw.gov.health.lmis.reports.domain.ReportCategory;

public class DashboardReportDataBuilder {
  private final UUID id = UUID.randomUUID();
  private String name = RandomStringUtils.random(6);
  private String url = "http://example.com";
  private ReportType type = ReportType.SUPERSET;
  private boolean enabled = true;
  private boolean showOnHomePage = false;
  private ReportCategory reportCategory = new ReportCategoryDataBuilder().buildAsNew();
  private final String rightName = (this.name + "_RIGHT").toUpperCase();

  public DashboardReportDataBuilder withName(String name) {
    this.name = name;
    return this;
  }

  public DashboardReportDataBuilder withUrl(String url) {
    this.url = url;
    return this;
  }

  public DashboardReportDataBuilder withType(ReportType type) {
    this.type = type;
    return this;
  }

  public DashboardReportDataBuilder withEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public DashboardReportDataBuilder withShowOnHomePage(boolean showOnHomePage) {
    this.showOnHomePage = showOnHomePage;
    return this;
  }

  public DashboardReportDataBuilder withCategory(ReportCategory reportCategory) {
    this.reportCategory = reportCategory;
    return this;
  }

  /**
   * Builds an instance of the {@link DashboardReport} class with populated ID.
   *
   * @return the instance of {@link DashboardReport} class
   */
  public DashboardReport build() {
    DashboardReport dashboardReport = buildAsNew();
    dashboardReport.setId(id);
    return dashboardReport;
  }

  /**
   * Build an instance of the {@link DashboardReport} class without ID field populated.
   *
   * @return the instance of {@link DashboardReport} class
   */
  public DashboardReport buildAsNew() {
    return new DashboardReport(
        name,
        url,
        type,
        enabled,
        showOnHomePage,
        reportCategory,
        rightName
    );
  }
}
