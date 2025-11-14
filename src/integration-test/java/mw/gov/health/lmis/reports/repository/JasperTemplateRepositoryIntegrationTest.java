package mw.gov.health.lmis.reports.repository;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import mw.gov.health.lmis.reports.domain.ReportCategory;
import mw.gov.health.lmis.reports.domain.JasperTemplate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class JasperTemplateRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<JasperTemplate> {

  private static final String NAME = "TemplateRepositoryIntegrationTest";

  @Autowired
  private JasperTemplateRepository jasperTemplateRepository;

  @Autowired
  private ReportCategoryRepository reportCategoryRepository;

  private ReportCategory reportCategory;

  @Override
  JasperTemplateRepository getRepository() {
    return this.jasperTemplateRepository;
  }

  @Override
  protected JasperTemplate generateInstance() {
    JasperTemplate jasperTemplate = new JasperTemplate();
    jasperTemplate.setName(NAME);
    jasperTemplate.setCategory(reportCategory);
    jasperTemplate.setIsDisplayed(true);
    return jasperTemplate;
  }

  @Before
  public void setUp() {
    jasperTemplateRepository.deleteAll();

    reportCategory = new ReportCategory();
    reportCategory.setName("Default Category");
    reportCategoryRepository.save(reportCategory);
  }

  @Test
  public void shouldFindTemplateByName() {
    JasperTemplate template = generateInstance();
    jasperTemplateRepository.save(template);

    JasperTemplate found = jasperTemplateRepository.findByName(NAME);

    assertThat("Template should not be null", found != null, is(true));
    assertThat(found.getName(), is(NAME));
  }

  @Test
  public void shouldFindTemplateByDisplayed() {
    JasperTemplate displayedTemplate = generateInstance();
    jasperTemplateRepository.save(displayedTemplate);

    JasperTemplate hiddenTemplate = new JasperTemplate();
    hiddenTemplate.setName("Hidden Template");
    hiddenTemplate.setCategory(reportCategory);
    hiddenTemplate.setIsDisplayed(false);
    jasperTemplateRepository.save(hiddenTemplate);

    List<JasperTemplate> displayed = jasperTemplateRepository.findByIsDisplayed(true);

    // Assert correct results
    assertThat(displayed.size(), is(1));
    assertThat(displayed.get(0).getIsDisplayed(), is(true));
    assertThat(displayed.get(0).getName(), is(NAME));
  }
}
