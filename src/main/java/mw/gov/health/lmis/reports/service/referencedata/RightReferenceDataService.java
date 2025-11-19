package mw.gov.health.lmis.reports.service.referencedata;

import static mw.gov.health.lmis.utils.RequestHelper.createUri;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.PostConstruct;
import mw.gov.health.lmis.reports.dto.external.RightDto;
import mw.gov.health.lmis.reports.exception.ValidationMessageException;
import mw.gov.health.lmis.reports.i18n.DashboardReportMessageKeys;
import mw.gov.health.lmis.utils.Message;
import mw.gov.health.lmis.utils.RequestHelper;
import mw.gov.health.lmis.utils.RequestParameters;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class RightReferenceDataService extends BaseReferenceDataService<RightDto> {

  @Override
  protected String getUrl() {
    return "/api/rights";
  }

  @Override
  protected Class<RightDto> getResultClass() {
    return RightDto.class;
  }

  @Override
  protected Class<RightDto[]> getArrayResultClass() {
    return RightDto[].class;
  }

  /**
   * Fix inherited rest template. The Jackson version mismatch by different dependencies causes
   * wrong content-type header to be sent to service. JasperReport 6.20.5 requires Jackson 2.14.2
   * but this version does not work with Spring 1.5.9. Once the Spring version is updated to 2.x
   * this method can be removed.
   */
  @PostConstruct
  public void fixInheritedRestTemplate() {
    if (this.restTemplate instanceof RestTemplate) {
      RestTemplate template = (RestTemplate) this.restTemplate;
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new JavaTimeModule());
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      MappingJackson2HttpMessageConverter jacksonConverter =
          new MappingJackson2HttpMessageConverter(mapper);
      template.setMessageConverters(Collections.singletonList(jacksonConverter));
    }
  }

  /**
   * Find a correct right by the provided name.
   *
   * @param name right name
   * @return right related with the name or {@code null}.
   */
  public RightDto findRight(String name) {
    List<RightDto> rights = findAll("/search", RequestParameters.init().set("name", name));
    return rights.isEmpty() ? null : rights.get(0);
  }

  /**
   * Save a new right by making a POST request to the referenced data service.
   *
   * @param rightDto the RightDto to save
   */
  @SuppressWarnings("PMD.PreserveStackTrace")
  public void save(RightDto rightDto) {
    String url = getServiceUrl() + getUrl();

    try {
      runWithTokenRetry(() ->
          restTemplate.exchange(
              createUri(url),
              HttpMethod.PUT,
              RequestHelper.createEntity(authorizationService.obtainAccessToken(), rightDto),
              RightDto.class
          ));
    } catch (HttpStatusCodeException ex) {
      throw new ValidationMessageException(
          new Message(DashboardReportMessageKeys.ERROR_COULD_NOT_SAVE_RIGHT, rightDto.getName()));
    }
  }

  /**
   * Delete an existing right by making a DELETE request to the referencedata service.
   *
   * @param rightId the ID of the RightDto to delete
   */
  @SuppressWarnings("PMD.PreserveStackTrace")
  public void delete(UUID rightId) {
    String url = getServiceUrl() + getUrl() + "/" + rightId;

    try {
      runWithTokenRetry(() ->
          restTemplate.exchange(
              createUri(url),
              HttpMethod.DELETE,
              RequestHelper.createEntity(authorizationService.obtainAccessToken(), null),
              Void.class
          )
      );
    } catch (HttpStatusCodeException ex) {
      throw new ValidationMessageException(
          new Message(DashboardReportMessageKeys.ERROR_COULD_NOT_DELETE_RIGHT, rightId, ex));
    }
  }
}
