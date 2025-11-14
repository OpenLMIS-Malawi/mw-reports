package mw.gov.health.lmis.reports.web;

import static mw.gov.health.lmis.reports.web.ReportCategoryController.RESOURCE_PATH;

import java.util.UUID;
import javax.validation.Valid;

import mw.gov.health.lmis.reports.dto.ReportCategoryDto;
import mw.gov.health.lmis.reports.service.ReportCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@Transactional
@RequestMapping(RESOURCE_PATH)
public class ReportCategoryController extends BaseController {

  public static final String RESOURCE_PATH = "/api/reports/reportCategories";

  @Autowired
  private ReportCategoryService reportCategoryService;

  /**
   * Get chosen report category.
   *
   * @param categoryId UUID of a report category we want to get.
   */
  @GetMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ReportCategoryDto getReportCategory(@PathVariable("id") UUID categoryId) {
    return reportCategoryService.getReportCategoryById(categoryId);
  }

  /**
   * Get page of all report categories.
   */
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<ReportCategoryDto> getAllReportCategories(Pageable pageable) {
    return reportCategoryService.getAllReportCategories(pageable);
  }

  /**
   * Create a new report category.
   *
   * @param dto Data transfer object containing Report Category data.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public ReportCategoryDto createReportCategory(@Valid @RequestBody ReportCategoryDto dto) {
    return reportCategoryService.createReportCategory(dto);
  }

  /**
   * Update an existing report category.
   *
   * @param id UUID of a report category to update.
   * @param dto Data transfer object containing Report Category data.
   */
  @PutMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ReportCategoryDto updateReportCategory(@PathVariable("id") UUID id,
      @Valid @RequestBody ReportCategoryDto dto) {
    return reportCategoryService.updateReportCategory(id, dto);
  }

  /**
   * Allows deleting a report category.
   *
   * @param categoryId UUID of a report category we want to delete.
   */
  @DeleteMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteReportCategory(@PathVariable("id") UUID categoryId) {
    reportCategoryService.deleteReportCategory(categoryId);
  }
}
