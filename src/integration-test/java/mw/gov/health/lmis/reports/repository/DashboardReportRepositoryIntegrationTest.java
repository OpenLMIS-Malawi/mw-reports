package mw.gov.health.lmis.reports.repository;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import mw.gov.health.lmis.reports.domain.DashboardReport;
import mw.gov.health.lmis.reports.domain.ReportCategory;
import mw.gov.health.lmis.utils.DashboardReportDataBuilder;
import mw.gov.health.lmis.utils.ReportCategoryDataBuilder;
import mw.gov.health.lmis.utils.ReportType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class DashboardReportRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<DashboardReport> {
  private static final String NAME = "DashboardReportIntegrationTest";
  private static final String UPDATED_NAME = "UPDATED_DashboardReportIntegrationTest";
  private static final String URL = "http://example.com";
  private static final String CATEGORY_NAME = "LMIS Forms";

  @Autowired
  private DashboardReportRepository dashboardReportRepository;

  @Autowired
  private JasperTemplateRepository jasperTemplateRepository;

  @Autowired
  private ReportCategoryRepository reportCategoryRepository;

  @Override
  DashboardReportRepository getRepository() {
    return this.dashboardReportRepository;
  }

  @Override
  protected DashboardReport generateInstance() {
    ReportCategory category = reportCategoryRepository
        .save(new ReportCategoryDataBuilder().buildAsNew());

    return new DashboardReportDataBuilder()
        .withName(NAME)
        .withUrl(URL)
        .withType(ReportType.POWERBI)
        .withEnabled(true)
        .withShowOnHomePage(false)
        .withCategory(category)
        .buildAsNew();
  }

  @Before
  public void setUp() {
    jasperTemplateRepository.deleteAll();
    dashboardReportRepository.deleteAll();
    reportCategoryRepository.deleteAll();
  }

  @Test
  public void shouldFindDashboardReportByName() {
    DashboardReport dashboardReport = dashboardReportRepository.save(generateInstance());
    boolean exists = dashboardReportRepository.existsByName(dashboardReport.getName());

    assertThat(exists, is(true));
  }

  @Test
  public void shouldFindAllDashboardReports() {
    Pageable pageable = new PageRequest(0, 3);
    ReportCategory defaultCategory = reportCategoryRepository.save(
        new ReportCategoryDataBuilder().buildAsNew()
    );

    DashboardReport dashboardReport1 = new DashboardReportDataBuilder()
        .withCategory(defaultCategory)
        .withShowOnHomePage(true)
        .build();
    DashboardReport dashboardReport2 = new DashboardReportDataBuilder()
        .withCategory(defaultCategory)
        .build();
    DashboardReport dashboardReport3 = new DashboardReportDataBuilder()
        .withCategory(defaultCategory)
        .build();

    dashboardReportRepository.save(dashboardReport1);
    dashboardReportRepository.save(dashboardReport2);
    dashboardReportRepository.save(dashboardReport3);

    Page<DashboardReport> page = dashboardReportRepository.findAll(pageable);
    List<DashboardReport> found = page.getContent();

    assertThat(found.size(), is(3));
    assertThat(page.getTotalElements(), is(3L));
  }

  @Test
  public void shouldDeleteDashboardReportAndLeaveCategory() {
    ReportCategory defaultCategory = reportCategoryRepository.save(
        new ReportCategoryDataBuilder().buildAsNew()
    );

    DashboardReport dashboardReport = new DashboardReportDataBuilder()
        .withCategory(defaultCategory)
        .build();

    dashboardReportRepository.save(dashboardReport);
    dashboardReportRepository.delete(dashboardReport);

    DashboardReport report = dashboardReportRepository.findOne(dashboardReport.getId());
    assertThat(report, is(nullValue()));

    ReportCategory category = reportCategoryRepository.findOne(defaultCategory.getId());
    assertThat(category, is(notNullValue()));
  }

  @Test
  public void shouldUpdateDashboardReport() {
    ReportCategory reportCategory = reportCategoryRepository.save(
        new ReportCategoryDataBuilder().buildAsNew()
    );
    DashboardReport dashboardReport = dashboardReportRepository.save(
        new DashboardReportDataBuilder().withName(NAME).withCategory(reportCategory).build()
    );

    assertThat(dashboardReport.getName(), is(NAME));
    assertThat(dashboardReport.getType(), is(ReportType.SUPERSET));
    assertThat(dashboardReport.getCategory().getId(), is(reportCategory.getId()));

    ReportCategory newReportCategory = reportCategoryRepository.save(
        new ReportCategoryDataBuilder().withName(CATEGORY_NAME).build()
    );

    dashboardReport.setName(UPDATED_NAME);
    dashboardReport.setType(ReportType.POWERBI);
    dashboardReport.setCategory(newReportCategory);

    dashboardReportRepository.save(dashboardReport);

    DashboardReport updatedReport = dashboardReportRepository.findOne(
        dashboardReport.getId());

    assertThat(updatedReport, is(notNullValue()));
    assertThat(updatedReport.getName(), is(UPDATED_NAME));
    assertThat(updatedReport.getType(), is(ReportType.POWERBI));
    assertThat(updatedReport.getCategory().getId(), is(newReportCategory.getId()));
  }

  @Test
  public void shouldSaveDashboardReport() {
    ReportCategory reportCategory = reportCategoryRepository.save(
        new ReportCategoryDataBuilder().buildAsNew()
    );
    DashboardReport dashboardReport = new DashboardReportDataBuilder().withName(NAME)
        .withCategory(reportCategory).build();
    DashboardReport savedReport = dashboardReportRepository.save(dashboardReport);

    assertThat(savedReport.getId(), is(notNullValue()));
    assertThat(savedReport.getName(), is(NAME));
  }

  @Test
  public void shouldNotFindNonExistentDashboardReportById() {
    DashboardReport foundReport = dashboardReportRepository.findOne(
        UUID.randomUUID());
    assertThat(foundReport, is(nullValue()));
  }

  @Test
  public void shouldNotFindNonExistentDashboardReportByName() {
    boolean exists = dashboardReportRepository
        .existsByName("Non-Existent name");

    assertThat(exists, is(false));
  }

  @Test
  public void shouldFindOnlyEnabledDashboardReports() {
    Pageable pageable = new PageRequest(0, 10);
    ReportCategory defaultCategory = reportCategoryRepository.save(
        new ReportCategoryDataBuilder().buildAsNew()
    );

    DashboardReport dashboardReport1 = new DashboardReportDataBuilder()
        .withCategory(defaultCategory)
        .withEnabled(false)
        .build();
    DashboardReport dashboardReport2 = new DashboardReportDataBuilder()
        .withCategory(defaultCategory)
        .withEnabled(false)
        .build();
    DashboardReport dashboardReport3 = new DashboardReportDataBuilder()
        .withCategory(defaultCategory)
        .build();

    dashboardReportRepository.save(dashboardReport1);
    dashboardReportRepository.save(dashboardReport2);
    dashboardReportRepository.save(dashboardReport3);

    Page<DashboardReport> page = dashboardReportRepository.findByEnabled(true, pageable);
    List<DashboardReport> found = page.getContent();

    assertThat(found.size(), is(1));
    assertThat(page.getTotalElements(), is(1L));
  }
}
