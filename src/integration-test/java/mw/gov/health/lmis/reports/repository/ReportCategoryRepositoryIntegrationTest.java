package mw.gov.health.lmis.reports.repository;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import mw.gov.health.lmis.utils.DashboardReportDataBuilder;
import mw.gov.health.lmis.utils.ReportCategoryDataBuilder;
import mw.gov.health.lmis.reports.domain.JasperTemplate;
import mw.gov.health.lmis.reports.domain.ReportCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@SuppressWarnings("PMD.TooManyMethods")
public class ReportCategoryRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<ReportCategory> {
  private static final String NAME = "ReportCategoryIntegrationTest";

  @Autowired
  private ReportCategoryRepository reportCategoryRepository;

  @Autowired
  private JasperTemplateRepository jasperTemplateRepository;

  @Autowired
  private DashboardReportRepository dashboardReportRepository;

  @Override
  ReportCategoryRepository getRepository() {
    return this.reportCategoryRepository;
  }

  @Override
  protected ReportCategory generateInstance() {
    return new ReportCategoryDataBuilder().withName(NAME).buildAsNew();
  }

  @Before
  public void setUp() {
    jasperTemplateRepository.deleteAll();
    dashboardReportRepository.deleteAll();
    reportCategoryRepository.deleteAll();
  }

  @Test
  public void shouldFindReportCategoryByName() {
    reportCategoryRepository.save(generateInstance());
    Optional<ReportCategory> foundCategory = reportCategoryRepository.findByName(NAME);

    assertThat(foundCategory.isPresent(), is(true));
    assertThat(foundCategory.get().getName(), is(NAME));
  }

  @Test
  public void shouldFindAllReportCategories() {
    // Clear all the data
    jasperTemplateRepository.deleteAll();
    dashboardReportRepository.deleteAll();
    reportCategoryRepository.deleteAll();

    ReportCategory reportCategory1 = reportCategoryRepository.save(
        new ReportCategoryDataBuilder().buildAsNew()
    );
    ReportCategory reportCategory2 = reportCategoryRepository.save(
        new ReportCategoryDataBuilder().buildAsNew()
    );

    reportCategoryRepository.save(reportCategory1);
    reportCategoryRepository.save(reportCategory2);

    Pageable pageable = new PageRequest(0, 2);
    Page<ReportCategory> page = reportCategoryRepository.findAll(pageable);
    List<ReportCategory> found = page.getContent();

    assertThat(found.size(), is(2));
    assertThat(page.getTotalElements(), is(2L));
  }

  @Test
  public void shouldDeleteReportCategory() {
    ReportCategory reportCategory = reportCategoryRepository.save(
        new ReportCategoryDataBuilder().buildAsNew()
    );

    reportCategoryRepository.delete(reportCategory.getId());

    ReportCategory foundCategory = reportCategoryRepository.findOne(
        reportCategory.getId());

    assertThat(foundCategory, is(nullValue()));
  }

  @Test
  public void shouldUpdateReportCategory() {
    ReportCategory reportCategory = reportCategoryRepository.save(
        new ReportCategoryDataBuilder().buildAsNew()
    );
    reportCategory.setName("UpdatedCategoryName");
    reportCategoryRepository.save(reportCategory);

    ReportCategory updatedCategory = reportCategoryRepository.findOne(
        reportCategory.getId());

    assertThat(updatedCategory, is(notNullValue()));
    assertThat(updatedCategory.getName(), is("UpdatedCategoryName"));
  }

  @Test
  public void shouldSaveReportCategory() {
    reportCategoryRepository.save(generateInstance());

    Optional<ReportCategory> foundCategory = reportCategoryRepository.findByName(NAME);

    assertThat(foundCategory.isPresent(), is(true));
    assertThat(foundCategory.get().getName(), is(NAME));
  }

  @Test
  public void shouldNotFindNonExistentReportCategoryById() {
    ReportCategory foundCategory = reportCategoryRepository.findOne(UUID.randomUUID());
    assertThat(foundCategory, is(nullValue()));
  }

  @Test
  public void shouldNotFindNonExistentReportCategoryByName() {
    Optional<ReportCategory> foundCategory = reportCategoryRepository
        .findByName("NonExistentCategory");

    assertThat(foundCategory.isPresent(), is(false));
  }

  @Test
  public void shouldReturnTrueWhenCategoryIsAssignedToDashboardReport() {
    ReportCategory reportCategory = reportCategoryRepository.save(
        new ReportCategoryDataBuilder().buildAsNew()
    );
    dashboardReportRepository.save(
        new DashboardReportDataBuilder().withCategory(reportCategory).build()
    );

    boolean exists = dashboardReportRepository.existsByCategory_Id(reportCategory.getId());
    assertThat(exists, is(true));
  }

  @Test
  public void shouldReturnFalseWhenCategoryIsNotAssignedToDashboardReport() {
    ReportCategory reportCategory = reportCategoryRepository.save(
        new ReportCategoryDataBuilder().buildAsNew()
    );
    boolean exists = dashboardReportRepository.existsByCategory_Id(reportCategory.getId());
    assertThat(exists, is(false));
  }

  @Test
  public void shouldReturnTrueWhenCategoryIsAssignedToJasperTemplate() {
    ReportCategory reportCategory = reportCategoryRepository.save(
        new ReportCategoryDataBuilder().buildAsNew()
    );

    JasperTemplate template = new JasperTemplate();
    template.setId(UUID.randomUUID());
    template.setName("name");
    template.setCategory(reportCategory);

    jasperTemplateRepository.save(template);

    boolean exists = jasperTemplateRepository.existsByCategory_Id(reportCategory.getId());
    assertThat(exists, is(true));
  }

  @Test
  public void shouldReturnFalseWhenCategoryIsNotAssignedToJasperTemplate() {
    ReportCategory reportCategory = reportCategoryRepository.save(
        new ReportCategoryDataBuilder().buildAsNew()
    );
    boolean exists = jasperTemplateRepository.existsByCategory_Id(reportCategory.getId());
    assertThat(exists, is(false));
  }
}
