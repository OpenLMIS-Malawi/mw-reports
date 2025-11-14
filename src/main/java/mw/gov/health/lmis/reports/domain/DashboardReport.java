package mw.gov.health.lmis.reports.domain;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mw.gov.health.lmis.utils.ReportType;

@Getter
@Setter
@Entity
@Table(name = "dashboard_reports")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DashboardReport extends BaseEntity {
  @Column(columnDefinition = TEXT_COLUMN_DEFINITION, nullable = false)
  private String name;

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION, nullable = false)
  private String url;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = TEXT_COLUMN_DEFINITION, nullable = false)
  private ReportType type;

  @Column(columnDefinition = BOOLEAN_COLUMN_DEFINITION, nullable = false)
  private boolean enabled;

  @Column(columnDefinition = BOOLEAN_COLUMN_DEFINITION, nullable = false)
  private boolean showOnHomePage;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "categoryid", referencedColumnName = "id", nullable = false)
  private ReportCategory category;

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION, nullable = false)
  private String rightName;

  /**
   * Create new instance of a Dashboard report based on data from {@link DashboardReport.Importer}.
   *
   * @param importer instance of {@link DashboardReport.Importer}.
   * @return new instance of a dashboard report.
   */
  public static DashboardReport newInstance(DashboardReport.Importer importer) {
    DashboardReport dashboardReport = new DashboardReport();
    dashboardReport.setId(importer.getId());
    dashboardReport.updateFrom(importer);

    return dashboardReport;
  }

  /**
   * Copy values of attributes into new or updated Dashboard Report.
   *
   * @param importer Dashboard report importer with new values.
   */
  public void updateFrom(DashboardReport.Importer importer) {
    this.name = importer.getName();
    this.url = importer.getUrl();
    this.type = importer.getType();
    this.enabled = importer.isEnabled();
    this.showOnHomePage = importer.isShowOnHomePage();
    this.category = importer.getCategory();
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to.
   */
  public void export(DashboardReport.Exporter exporter) {
    exporter.setId(id);
    exporter.setName(name);
    exporter.setUrl(url);
    exporter.setType(type);
    exporter.setEnabled(enabled);
    exporter.setShowOnHomePage(showOnHomePage);
    exporter.setCategory(category);
    exporter.setRightName(rightName);
  }

  public interface Exporter {
    void setId(UUID id);

    void setName(String name);

    void setUrl(String url);

    void setType(ReportType type);

    void setEnabled(boolean enabled);

    void setShowOnHomePage(boolean showOnHomePage);

    void setCategory(ReportCategory category);

    void setRightName(String name);
  }

  public interface Importer {
    UUID getId();

    String getName();

    String getUrl();

    ReportType getType();

    boolean isEnabled();

    boolean isShowOnHomePage();

    ReportCategory getCategory();

    String getRightName();
  }
}