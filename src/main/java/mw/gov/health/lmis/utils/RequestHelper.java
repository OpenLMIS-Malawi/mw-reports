package mw.gov.health.lmis.utils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import mw.gov.health.lmis.reports.exception.EncodingException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

public final class RequestHelper {

  private RequestHelper() {
    throw new UnsupportedOperationException();
  }

  /**
   * Creates a {@link URI} from the given string representation without any parameters.
   */
  public static URI createUri(String url) {
    return createUri(url, null);
  }

  /**
   * Creates a {@link URI} from the given string representation and with the given parameters.
   */
  public static URI createUri(String url, RequestParameters parameters) {
    UriComponentsBuilder builder = UriComponentsBuilder.newInstance().uri(URI.create(url));

    RequestParameters
        .init()
        .setAll(parameters)
        .forEach(e -> e.getValue().forEach(one -> {
              try {
                builder.queryParam(e.getKey(),
                    UriUtils.encodeQueryParam(String.valueOf(one),
                        StandardCharsets.UTF_8.name()));
              } catch (UnsupportedEncodingException ex) {
                throw new EncodingException(ex);
              }
            }
        ));

    return builder.build(true).toUri();
  }

  /**
   * Creates an {@link HttpEntity} with the given payload as a body and adds an authorization
   * header with the provided token.
   * @param payload the body of the request, pass null if no body
   * @param token the token to put into the authorization header
   * @param <E> the type of the body for the request
   * @return the {@link HttpEntity} to use
   */
  public static <E> HttpEntity<E> createEntity(String token, E payload) {
    if (payload == null) {
      return createEntity(token);
    } else {
      return new HttpEntity<>(payload, createHeadersWithAuth(token));
    }
  }

  /**
   * Creates an {@link HttpEntity} and adds an authorizatior header with the provided token.
   * @param token the token to put into the authorization header
   * @return the {@link HttpEntity} to use
   */
  public static HttpEntity createEntity(String token) {
    return new HttpEntity(createHeadersWithAuth(token));
  }

  private static HttpHeaders createHeadersWithAuth(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    return headers;
  }
}