package mw.gov.health.lmis.reports.web;

import java.util.Collections;
import java.util.Map;
import mw.gov.health.lmis.reports.exception.NotFoundMessageException;
import mw.gov.health.lmis.reports.i18n.SupersetMessageKeys;
import mw.gov.health.lmis.reports.repository.DashboardReportRepository;
import mw.gov.health.lmis.reports.service.SupersetService;
import mw.gov.health.lmis.reports.service.ViewPermissionService;
import mw.gov.health.lmis.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/api/reports/superset")
public class SupersetGuestTokenController extends BaseController {

  @Autowired
  private SupersetService supersetService;

  @Autowired
  private DashboardReportRepository dashboardReportRepository;

  @Autowired
  private ViewPermissionService viewPermissionService;

  /**
   * Get a Superset guest token for embedding a dashboard.
   *
   * @param embeddedUuid the embedded UUID of the Superset dashboard
   * @return a map containing the guest token
   */
  @GetMapping("/guest-token")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, String> getGuestToken(@RequestParam final String embeddedUuid) {
    viewPermissionService.canViewReports(null);

    if (!dashboardReportRepository.existsByEmbeddedUuid(embeddedUuid)) {
      throw new NotFoundMessageException(
          new Message(SupersetMessageKeys.ERROR_SUPERSET_DASHBOARD_NOT_FOUND, embeddedUuid));
    }

    String username = SecurityContextHolder.getContext().getAuthentication().getName();

    String token = supersetService.getGuestToken(
        embeddedUuid, username, username, username
    );

    return Collections.singletonMap("token", token);
  }
}
