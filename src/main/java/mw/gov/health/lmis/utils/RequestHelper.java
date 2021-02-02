package mw.gov.health.lmis.utils;

import org.apache.commons.codec.Charsets;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;

import mw.gov.health.lmis.reports.exception.EncodingException;

public final class RequestHelper {

  private RequestHelper() {
    throw new UnsupportedOperationException();
  }

  /**
   * Creates a {@link URI} from the given string representation and with the given parameters.
   */
  public static URI createUri(String url, RequestParameters parameters) {
    UriComponentsBuilder builder = UriComponentsBuilder.newInstance().uri(URI.create(url));

    parameters.forEach(e -> {
      try {
        builder.queryParam(e.getKey(),
            UriUtils.encodeQueryParam(String.valueOf(e.getValue()), Charsets.UTF_8.name()));
      } catch (UnsupportedEncodingException ex) {
        throw new EncodingException(ex);
      }
    });

    return builder.build(true).toUri();
  }

}
