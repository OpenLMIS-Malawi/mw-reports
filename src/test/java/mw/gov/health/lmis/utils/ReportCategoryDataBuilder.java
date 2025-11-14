package mw.gov.health.lmis.utils;

import java.util.UUID;
import org.apache.commons.lang.RandomStringUtils;
import mw.gov.health.lmis.reports.domain.ReportCategory;

public class ReportCategoryDataBuilder {
  private final UUID id = UUID.randomUUID();
  private String name = RandomStringUtils.random(6);

  public ReportCategoryDataBuilder withName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Builds an instance of the {@link ReportCategory} class with populated ID.
   *
   * @return the instance of {@link ReportCategory} class
   */
  public ReportCategory build() {
    ReportCategory reportCategory = buildAsNew();
    reportCategory.setId(id);
    return reportCategory;
  }

  /**
   * Build an instance of the {@link ReportCategory} class without ID field populated.
   *
   * @return the instance of {@link ReportCategory} class
   */
  public ReportCategory buildAsNew() {
    return new ReportCategory(name);
  }

}
