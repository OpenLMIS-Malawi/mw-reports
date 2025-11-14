package mw.gov.health.lmis.reports.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import mw.gov.health.lmis.utils.ReportCategoryDataBuilder;
import mw.gov.health.lmis.reports.dto.ReportCategoryDto;

public class ReportCategoryTest {

  @Test
  public void shouldCreateNewInstance() {
    ReportCategory reportCategory = new ReportCategoryDataBuilder().build();
    ReportCategoryDto importer = ReportCategoryDto.newInstance(reportCategory);
    ReportCategory newInstance = ReportCategory.newInstance(importer);

    assertThat(importer.getName()).isEqualTo(newInstance.getName());
  }

  @Test
  public void shouldExportData() {
    ReportCategory reportCategory = new ReportCategoryDataBuilder().build();
    ReportCategoryDto dto = new ReportCategoryDto();
    reportCategory.export(dto);

    assertThat(dto.getName()).isEqualTo(reportCategory.getName());
  }

  @Test
  public void shouldBeEqualIfSameFields() {
    ReportCategoryDto dto = new ReportCategoryDto();
    dto.setName("Test Category");

    ReportCategory reportCategory1 = ReportCategory.newInstance(dto);
    ReportCategory reportCategory2 = ReportCategory.newInstance(dto);

    assertThat(reportCategory1).isEqualTo(reportCategory2);
    assertThat(reportCategory1.hashCode()).isEqualTo(reportCategory2.hashCode());
  }
}
