package mw.gov.health.lmis.reports.service;

import java.util.UUID;
import mw.gov.health.lmis.reports.domain.ReportCategory;
import mw.gov.health.lmis.reports.dto.ReportCategoryDto;
import mw.gov.health.lmis.reports.exception.NotFoundMessageException;
import mw.gov.health.lmis.reports.exception.ValidationMessageException;
import mw.gov.health.lmis.reports.i18n.ReportCategoryMessageKeys;
import mw.gov.health.lmis.reports.repository.DashboardReportRepository;
import mw.gov.health.lmis.reports.repository.JasperTemplateRepository;
import mw.gov.health.lmis.reports.repository.ReportCategoryRepository;
import mw.gov.health.lmis.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ReportCategoryService {
  private final ReportCategoryRepository reportCategoryRepository;
  private final DashboardReportRepository dashboardReportRepository;
  private final JasperTemplateRepository jasperTemplateRepository;
  private final PermissionService permissionService;

  /**
   * Constructs a new {@link ReportCategoryService} with the specified dependencies.
   *
   * @param reportCategoryRepository The repository responsible for handling report category data.
   * @param permissionService The service responsible for checking and managing user permissions.
   * @param dashboardReportRepository The repository for managing dashboard reports.
   * @param jasperTemplateRepository The repository for managing Jasper templates.
   */
  @Autowired
  public ReportCategoryService(ReportCategoryRepository reportCategoryRepository,
      PermissionService permissionService, DashboardReportRepository dashboardReportRepository,
      JasperTemplateRepository jasperTemplateRepository) {
    this.reportCategoryRepository = reportCategoryRepository;
    this.dashboardReportRepository = dashboardReportRepository;
    this.jasperTemplateRepository = jasperTemplateRepository;
    this.permissionService = permissionService;
  }

  /**
   * Get chosen report category.
   *
   * @param categoryId UUID of a report category we want to get.
   */
  public ReportCategoryDto getReportCategoryById(UUID categoryId) {
    permissionService.canManageReportCategories();

    ReportCategory reportCategory = reportCategoryRepository.findOne(categoryId);

    if (reportCategory == null) {
      throw new NotFoundMessageException(ReportCategoryMessageKeys.ERROR_REPORT_CATEGORY_NOT_FOUND);
    }

    return ReportCategoryDto.newInstance(reportCategory);
  }

  /**
   * Get page of all report categories.
   */
  public Page<ReportCategoryDto> getAllReportCategories(Pageable pageable) {
    Page<ReportCategory> reportCategories = reportCategoryRepository.findAll(pageable);
    return reportCategories.map(ReportCategoryDto::newInstance);
  }

  /**
   * Create a new report category.
   *
   * @param dto Data transfer object containing Report Category data.
   */
  public ReportCategoryDto createReportCategory(ReportCategoryDto dto) {
    permissionService.canManageReportCategories();

    boolean categoryExists = reportCategoryRepository.existsByName(dto.getName());
    if (categoryExists) {
      throw new ValidationMessageException(new Message(
          ReportCategoryMessageKeys.ERROR_REPORT_CATEGORY_NAME_DUPLICATED, dto.getName()));
    }

    ReportCategory reportCategoryToSave = ReportCategory.newInstance(dto);
    ReportCategory savedCategory = reportCategoryRepository.save(reportCategoryToSave);

    return ReportCategoryDto.newInstance(savedCategory);
  }

  /**
   * Update an existing report category.
   *
   * @param id UUID of a report category to update.
   * @param dto Data transfer object containing Report Category data.
   */
  public ReportCategoryDto updateReportCategory(UUID id, ReportCategoryDto dto) {
    permissionService.canManageReportCategories();

    if (dto.getId() != null && !dto.getId().equals(id)) {
      throw new ValidationMessageException(new Message(
          ReportCategoryMessageKeys.ERROR_REPORT_CATEGORY_ID_MISMATCH));
    }

    boolean nameAlreadyExists = reportCategoryRepository.existsByIdIsNotAndName(id, dto.getName());
    if (nameAlreadyExists) {
      throw new ValidationMessageException(
          new Message(ReportCategoryMessageKeys.ERROR_REPORT_CATEGORY_NAME_DUPLICATED));
    }

    ReportCategory existingCategory = reportCategoryRepository.findOne(id);
    if (existingCategory == null) {
      throw new NotFoundMessageException(
          new Message(ReportCategoryMessageKeys.ERROR_REPORT_CATEGORY_NOT_FOUND, id));
    }
    existingCategory.updateFrom(dto);
    return ReportCategoryDto.newInstance(reportCategoryRepository.save(existingCategory));
  }

  /**
   * Allows deleting a report category.
   *
   * @param categoryId UUID of a report category we want to delete.
   */
  public void deleteReportCategory(UUID categoryId) {
    permissionService.canManageReportCategories();

    boolean isAssignedToReports = dashboardReportRepository.existsByCategory_Id(categoryId);
    boolean isAssignedToTemplates = jasperTemplateRepository.existsByCategory_Id(categoryId);

    if (isAssignedToReports || isAssignedToTemplates) {
      throw new ValidationMessageException(new Message(
          ReportCategoryMessageKeys.ERROR_CATEGORY_ALREADY_ASSIGNED, categoryId));
    }

    ReportCategory reportCategory = reportCategoryRepository.findOne(categoryId);
    if (reportCategory == null) {
      throw new NotFoundMessageException(new Message(
          ReportCategoryMessageKeys.ERROR_REPORT_CATEGORY_NOT_FOUND, categoryId));
    }
    reportCategoryRepository.delete(reportCategory);
  }

}
