package mw.gov.health.lmis.reports.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import mw.gov.health.lmis.reports.domain.DashboardReport;
import mw.gov.health.lmis.reports.domain.ReportCategory;
import mw.gov.health.lmis.reports.dto.DashboardReportDto;
import mw.gov.health.lmis.reports.dto.external.RightDto;
import mw.gov.health.lmis.reports.dto.external.RightType;
import mw.gov.health.lmis.reports.exception.NotFoundMessageException;
import mw.gov.health.lmis.reports.exception.ValidationMessageException;
import mw.gov.health.lmis.reports.i18n.DashboardReportMessageKeys;
import mw.gov.health.lmis.reports.i18n.ReportCategoryMessageKeys;
import mw.gov.health.lmis.reports.repository.DashboardReportRepository;
import mw.gov.health.lmis.reports.repository.ReportCategoryRepository;
import mw.gov.health.lmis.reports.service.referencedata.RightReferenceDataService;
import mw.gov.health.lmis.utils.Message;
import mw.gov.health.lmis.utils.Pagination;
import mw.gov.health.lmis.utils.PropertyKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DashboardReportService {
  private final ReportCategoryRepository reportCategoryRepository;
  private final DashboardReportRepository dashboardReportRepository;
  private final RightReferenceDataService rightReferenceDataService;
  private final PermissionService permissionService;
  private final ViewPermissionService viewPermissionService;

  /**
   * Constructs a new {@link ReportCategoryService} with the specified dependencies.
   *
   * @param reportCategoryRepository The repository responsible for handling report category data.
   * @param permissionService The service responsible for checking and managing user permissions.
   * @param dashboardReportRepository The repository for managing dashboard reports.
   */
  @Autowired
  public DashboardReportService(ReportCategoryRepository reportCategoryRepository,
      PermissionService permissionService, DashboardReportRepository dashboardReportRepository,
      RightReferenceDataService rightReferenceDataService,
      ViewPermissionService viewPermissionService) {
    this.reportCategoryRepository = reportCategoryRepository;
    this.dashboardReportRepository = dashboardReportRepository;
    this.rightReferenceDataService = rightReferenceDataService;
    this.permissionService = permissionService;
    this.viewPermissionService = viewPermissionService;
  }

  /**
   * Retrieves a dashboard report by its ID.
   *
   * @param reportId UUID of the dashboard report to retrieve.
   * @return DashboardReportDto containing the dashboard report details.
   */
  public DashboardReportDto getDashboardReport(UUID reportId) {
    permissionService.canManageReports();

    DashboardReport dashboardReport = dashboardReportRepository.findOne(reportId);

    if (dashboardReport == null) {
      throw new NotFoundMessageException(new Message(
          DashboardReportMessageKeys.ERROR_DASHBOARD_REPORT_NOT_FOUND, reportId));
    }

    return DashboardReportDto.newInstance(dashboardReport);
  }

  /**
   * Retrieves a page of dashboard reports, optionally filtered by the showOnHomePage parameter.
   *
   * @param pageable Pageable parameters for pagination.
   * @return A page of dashboard reports matching the criteria.
   */
  public Page<DashboardReportDto> getDashboardReports(Pageable pageable) {
    permissionService.canManageReports();
    Page<DashboardReport> dashboardReports = dashboardReportRepository.findAll(pageable);
    return Pagination.getPage(dashboardReports.map(DashboardReportDto::newInstance),pageable);
  }

  /**
   * Retrieves a page of dashboard reports for which user has rights, or retrieves
   * a home page report.
   *
   * @param pageable Pageable parameters for pagination.
   * @param showOnHomePage Filter to only include report that is shown on the home page.
   * @return A page of dashboard reports matching the criteria.
   */
  public Page<DashboardReportDto> getDashboardReportsForUser(Pageable pageable,
      boolean showOnHomePage) {
    if (!showOnHomePage) {
      viewPermissionService.canViewReports(null);
    }

    List<String> accessibleRights = permissionService.filterRightsForUser(
        dashboardReportRepository.findByEnabled(true, pageable).getContent().stream()
            .map(DashboardReport::getRightName)
            .filter(Objects::nonNull)
            .collect(Collectors.toList())
    );

    if (accessibleRights.isEmpty()) {
      return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }

    Page<DashboardReport> dashboardReports = dashboardReportRepository
        .findByRightsAndShowOnHomePage(
            accessibleRights,
            showOnHomePage ? Boolean.TRUE : null,
            pageable
        );

    return Pagination.getPage(dashboardReports.map(DashboardReportDto::newInstance), pageable);
  }

  /**
   * Deletes the dashboard report with the given ID.
   *
   * @param reportId UUID of the dashboard report to delete.
   * @throws NotFoundMessageException if no dashboard report with the given ID is found.
   */
  public void deleteDashboardReport(UUID reportId) {
    permissionService.canManageReports();

    DashboardReport dashboardReport = dashboardReportRepository.findOne(reportId);

    if (dashboardReport == null) {
      throw new NotFoundMessageException(new Message(
          DashboardReportMessageKeys.ERROR_DASHBOARD_REPORT_NOT_FOUND, reportId));
    }

    RightDto foundRight = rightReferenceDataService.findRight(dashboardReport.getRightName());
    rightReferenceDataService.delete(foundRight.getId());
    dashboardReportRepository.delete(dashboardReport);
  }

  /**
   * Adds a new dashboard report to the database.
   *
   * @param dto Data transfer object containing Dashboard Report data.
   * @return Saved dashboard report.
   */
  public DashboardReportDto createDashboardReport(DashboardReportDto dto) {
    permissionService.canManageReports();

    boolean reportExists = dashboardReportRepository.existsByName(dto.getName());
    if (reportExists) {
      throw new ValidationMessageException(new Message(
          DashboardReportMessageKeys.ERROR_DASHBOARD_REPORT_NAME_DUPLICATED, dto.getName()));
    }

    DashboardReport dashboardReportToCreate = new DashboardReport();
    updateFrom(dto, dashboardReportToCreate);
    dashboardReportToCreate.setId(null);

    RightDto rightToSave = createRight(dashboardReportToCreate.getName());
    dashboardReportToCreate.setRightName(rightToSave.getName());

    try {
      rightReferenceDataService.save(rightToSave);
    } catch (Exception ex) {
      throw new ValidationMessageException(new Message(
          DashboardReportMessageKeys.ERROR_COULD_NOT_SAVE_RIGHT), ex);
    }

    try {
      return DashboardReportDto.newInstance(saveDashboardReport(dashboardReportToCreate));
    } catch (DataIntegrityViolationException ex) {
      throw new ValidationMessageException(new Message(
          DashboardReportMessageKeys.ERROR_DASHBOARD_REPORT_NAME_DUPLICATED, dto.getName()), ex);
    }
  }

  /**
   * Updates a dashboard report in the database.
   *
   * @param id The UUID of the dashboard report to update.
   * @param dashboardReportDto The DTO containing the updated dashboard report data.
   * @return The updated DashboardReportDto.
   */
  public DashboardReportDto updateDashboardReport(UUID id, DashboardReportDto dashboardReportDto) {
    permissionService.canManageReports();

    if (dashboardReportDto.getId() != null && !Objects.equals(dashboardReportDto.getId(), id)) {
      throw new ValidationMessageException(new Message(
          DashboardReportMessageKeys.ERROR_DASHBOARD_REPORT_ID_MISMATCH));
    }

    DashboardReport dashboardReport = dashboardReportRepository.findOne(id);

    if (dashboardReport == null) {
      throw new NotFoundMessageException(new Message(
          DashboardReportMessageKeys.ERROR_DASHBOARD_REPORT_NOT_FOUND, id));
    }

    updateFrom(dashboardReportDto, dashboardReport);
    saveDashboardReport(dashboardReport);

    return DashboardReportDto.newInstance(dashboardReport);
  }

  /**
   * Update a dashboard report with the data from DTO.
   *
   * @param newReport The DashboardReportDto containing the category ID.
   * @param reportToUpdate Report which is being updated.
   */
  private void updateFrom(DashboardReportDto newReport, DashboardReport reportToUpdate) {
    reportToUpdate.updateFrom(newReport);
    reportToUpdate.setCategory(getCategoryOrThrow(newReport));
  }

  /**
   * Retrieves the ReportCategory from the database using the category ID
   * from the provided DTO. If the category doesn't exist, throws a
   * NotFoundMessageException.
   *
   * @param newReport The DashboardReportDto containing the category ID
   * @return The corresponding ReportCategory
   * @throws NotFoundMessageException if the category doesn't exist
   */
  private ReportCategory getCategoryOrThrow(DashboardReportDto newReport) {
    if (newReport.getCategory() == null) {
      throw new NotFoundMessageException(new Message(
          ReportCategoryMessageKeys.ERROR_REPORT_CATEGORY_NOT_FOUND));
    }

    ReportCategory reportCategory = reportCategoryRepository.findOne(
        newReport.getCategory().getId());

    if (reportCategory == null) {
      throw new NotFoundMessageException(new Message(
          ReportCategoryMessageKeys.ERROR_REPORT_CATEGORY_NOT_FOUND));
    }

    return reportCategory;
  }

  /**
   * Creates right for new dashboard report.
   *
   * @param dashboardReportName Name of the dashboard report used to create right name
   * @return New RightDto
   */
  private RightDto createRight(String dashboardReportName) {
    String transformedName = PropertyKeyUtil.transformToPropertyKey(dashboardReportName);

    if (null == transformedName) {
      throw new ValidationMessageException(new Message(
          DashboardReportMessageKeys.ERROR_COULD_NOT_SAVE_RIGHT));
    }

    RightDto rightToSave = new RightDto();
    rightToSave.setName(transformedName);
    rightToSave.setType(RightType.REPORTS);
    return rightToSave;
  }

  /**
   * Saves dashboard report and validates if any
   * report has showOnHomepage set to true and overrides it.
   */
  public DashboardReport saveDashboardReport(DashboardReport dashboardReport) {
    DashboardReport dashboardReportToReturn = dashboardReportRepository.save(dashboardReport);

    if (dashboardReport.isShowOnHomePage()) {
      List<DashboardReport> existingReports = dashboardReportRepository.findByShowOnHomePage(true);

      existingReports.stream()
          .filter(report -> !report.getId().equals(dashboardReportToReturn.getId()))
          .forEach(report -> {
            report.setShowOnHomePage(false);
            dashboardReportRepository.save(report);
          });
    }

    return dashboardReportToReturn;
  }
}
