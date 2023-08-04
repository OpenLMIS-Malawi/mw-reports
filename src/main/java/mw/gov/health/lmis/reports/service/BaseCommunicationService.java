package mw.gov.health.lmis.reports.service;


import static mw.gov.health.lmis.reports.service.AuthorizationService.ACCESS_TOKEN;
import static mw.gov.health.lmis.utils.RequestHelper.createUri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import mw.gov.health.lmis.reports.dto.external.ResultDto;
import mw.gov.health.lmis.reports.exception.DataRetrievalException;
import mw.gov.health.lmis.utils.DynamicPageTypeReference;
import mw.gov.health.lmis.utils.DynamicResultDtoTypeReference;
import mw.gov.health.lmis.utils.PageImplRepresentation;
import mw.gov.health.lmis.utils.RequestParameters;

@SuppressWarnings("PMD.TooManyMethods")
public abstract class BaseCommunicationService<T> {
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected RestOperations restTemplate = new RestTemplate();

  protected AuthorizationService authorizationService;

  protected abstract String getServiceUrl();

  protected abstract String getUrl();

  protected abstract Class<T> getResultClass();

  protected abstract Class<T[]> getArrayResultClass();

  @Autowired
  public void setAuthorizationService(AuthorizationService authorizationService) {
    this.authorizationService = authorizationService;
  }

  /**
   * Return one object from service.
   *
   * @param id UUID_COLUMN_DEFINITION of requesting object.
   * @return Requesting reference data object.
   */
  public T findOne(UUID id) {
    return findOne(id.toString(), RequestParameters.init());
  }

  /**
   * Return one object from service.
   *
   * @param resourceUrl Endpoint url.
   * @param parameters  Map of query parameters.
   * @return one reference data T objects.
   */
  public T findOne(String resourceUrl, RequestParameters parameters) {
    return findOne(resourceUrl, parameters, getResultClass());
  }

  /**
   * Return one object from service.
   *
   * @param resourceUrl Endpoint url.
   * @param parameters  Map of query parameters.
   * @param type        set to what type a response should be converted.
   * @return one reference data T objects.
   */
  public <P> P findOne(String resourceUrl, RequestParameters parameters, Class<P> type) {
    String url = getServiceUrl() + getUrl() + resourceUrl;

    RequestParameters params = RequestParameters
        .init()
        .setAll(parameters)
        .set(ACCESS_TOKEN, authorizationService.obtainAccessToken());

    try {
      return restTemplate
          .getForEntity(createUri(url, params), type)
          .getBody();
    } catch (HttpStatusCodeException ex) {
      // rest template will handle 404 as an exception, instead of returning null
      if (HttpStatus.NOT_FOUND == ex.getStatusCode()) {
        logger.warn(
            "{} matching params does not exist. Params: {}",
            getResultClass().getSimpleName(), parameters
        );

        return null;
      }

      throw buildDataRetrievalException(ex);
    }
  }

  public List<T> findAll() {
    return findAll("");
  }

  public List<T> findAll(String resourceUrl) {
    return findAll(resourceUrl, getArrayResultClass());
  }

  public <P> List<P> findAll(String resourceUrl, Class<P[]> type) {
    return findAll(resourceUrl, RequestParameters.init(), type);
  }

  /**
   * Return all reference data T objects.
   *
   * @param resourceUrl Endpoint url.
   * @param parameters  Map of query parameters.
   * @return all reference data T objects.
   */
  public List<T> findAll(String resourceUrl, RequestParameters parameters) {
    return findAll(resourceUrl, parameters, getArrayResultClass());
  }

  public <P> List<P> findAll(String resourceUrl, RequestParameters parameters, Class<P[]> type) {
    return findAll(resourceUrl, parameters, null, HttpMethod.GET, type);
  }

  /**
   * Return all reference data T objects that need to be retrieved with POST request.
   *
   * @param resourceUrl Endpoint url.
   * @param parameters  Map of query parameters.
   * @param payload     body to include with the outgoing request.
   * @return all reference data T objects.
   */
  protected List<T> findAll(String resourceUrl, RequestParameters parameters,
                            Object payload) {
    return findAll(resourceUrl, parameters, payload, HttpMethod.POST, getArrayResultClass());
  }

  protected <P> List<P> findAll(String resourceUrl, RequestParameters parameters,
                          Object payload, HttpMethod method, Class<P[]> type) {
    String url = getServiceUrl() + getUrl() + resourceUrl;

    RequestParameters params = RequestParameters
        .init()
        .setAll(parameters)
        .set(ACCESS_TOKEN, authorizationService.obtainAccessToken());

    try {
      ResponseEntity<P[]> response;

      if (HttpMethod.GET == method) {
        response = restTemplate
            .getForEntity(createUri(url, params), type);
      } else {
        response = restTemplate
            .postForEntity(createUri(url, params), payload, type);
      }

      return Stream.of(response.getBody()).collect(Collectors.toList());
    } catch (HttpStatusCodeException ex) {
      throw buildDataRetrievalException(ex);
    }
  }

  protected Page<T> getPage(String resourceUrl, RequestParameters parameters) {
    return getPage(resourceUrl, parameters, null, HttpMethod.GET, getResultClass());
  }

  /**
   * Return all reference data T objects for Page that need to be retrieved with POST request.
   *
   * @param resourceUrl Endpoint url.
   * @param parameters  Map of query parameters.
   * @param payload     body to include with the outgoing request.
   * @return Page of reference data T objects.
   */
  protected Page<T> getPage(String resourceUrl, RequestParameters parameters, Object payload) {
    return getPage(resourceUrl, parameters, payload, HttpMethod.POST, getResultClass());
  }

  /**
   * Return all reference data T objects for Page that need to be retrieved with GET request.
   *
   * @param parameters  Map of query parameters.
   * @return Page of reference data T objects.
   */
  public Page<T> getPage(RequestParameters parameters) {
    return getPage("", parameters, null, HttpMethod.GET, getResultClass());
  }

  protected <P> Page<P> getPage(String resourceUrl, RequestParameters parameters, Object payload,
                                HttpMethod method, Class<P> type) {
    String url = getServiceUrl() + getUrl() + resourceUrl;
    RequestParameters params = RequestParameters
        .init()
        .setAll(parameters)
        .set(ACCESS_TOKEN, authorizationService.obtainAccessToken());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    try {
      ResponseEntity<PageImplRepresentation<P>> response = restTemplate.exchange(
              createUri(url, params),
              method,
              (payload != null) ? new HttpEntity<>(payload, headers) : null,
              new DynamicPageTypeReference<>(type)
      );
      return response.getBody();

    } catch (HttpStatusCodeException ex) {
      throw buildDataRetrievalException(ex);
    }
  }

  protected <P> ResultDto<P> getResult(String resourceUrl, RequestParameters parameters,
                                       Class<P> type) {
    String url = getServiceUrl() + getUrl() + resourceUrl;
    RequestParameters params = RequestParameters
        .init()
        .setAll(parameters)
        .set(ACCESS_TOKEN, authorizationService.obtainAccessToken());

    ResponseEntity<ResultDto<P>> response = restTemplate.exchange(
        createUri(url, params),
        HttpMethod.GET,
        null,
        new DynamicResultDtoTypeReference<>(type)
    );

    return response.getBody();
  }

  protected void setRestTemplate(RestOperations template) {
    this.restTemplate = template;
  }

  private DataRetrievalException buildDataRetrievalException(HttpStatusCodeException ex) {
    return new DataRetrievalException(getResultClass().getSimpleName(),
        ex.getStatusCode(),
        ex.getResponseBodyAsString());
  }
}
