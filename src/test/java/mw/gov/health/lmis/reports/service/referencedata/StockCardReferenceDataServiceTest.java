package mw.gov.health.lmis.reports.service.referencedata;

import static mw.gov.health.lmis.reports.service.referencedata.StockCardReferenceDataService.MAX_IDS_PER_REQUEST;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import mw.gov.health.lmis.reports.dto.external.StockCardDto;
import mw.gov.health.lmis.reports.service.AuthorizationService;
import mw.gov.health.lmis.utils.PageImplRepresentation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class StockCardReferenceDataServiceTest {

  // nginx rejects a request line longer than 8 KB with 414
  private static final int MAX_REQUEST_LINE_LENGTH = 8192;

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private AuthorizationService authorizationService;

  @Captor
  private ArgumentCaptor<URI> uriCaptor;

  private StockCardReferenceDataService service;

  @Before
  public void setUp() {
    service = new StockCardReferenceDataService();
    service.setAuthorizationService(authorizationService);
    ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
    ReflectionTestUtils.setField(service, "referenceDataUrl", "http://localhost");
    when(authorizationService.obtainAccessToken()).thenReturn(UUID.randomUUID().toString());
  }

  @Test
  public void shouldNotCallStockManagementWhenIdsAreEmpty() {
    // when
    List<StockCardDto> found = service.findByIds(Collections.emptySet());

    // then
    assertThat(found, is(empty()));
    verifyZeroInteractions(restTemplate);
  }

  @Test
  public void shouldNotCallStockManagementWhenIdsAreNullOrContainOnlyNulls() {
    // when
    List<StockCardDto> foundForNull = service.findByIds(null);
    List<StockCardDto> foundForNullIds = service.findByIds(Arrays.asList((UUID) null, null));

    // then
    assertThat(foundForNull, is(empty()));
    assertThat(foundForNullIds, is(empty()));
    verifyZeroInteractions(restTemplate);
  }

  @Test
  public void shouldRequestStockCardsInBatches() {
    // given
    Set<UUID> ids = randomIds(MAX_IDS_PER_REQUEST + 1);
    StockCardDto first = randomCard();
    StockCardDto second = randomCard();
    whenStockCardsAreRequested().thenReturn(pageOf(first), pageOf(second));

    // when
    final List<StockCardDto> found = service.findByIds(ids);

    // then
    List<URI> uris = captureRequests(2);
    List<UUID> requestedIds = new ArrayList<>();
    assertBatch(uris.get(0), MAX_IDS_PER_REQUEST, requestedIds);
    assertBatch(uris.get(1), 1, requestedIds);

    assertThat(requestedIds.size(), is(ids.size()));
    assertThat(new HashSet<>(requestedIds), is(ids));
    assertThat(found, contains(first, second));
  }

  @Test
  public void shouldSendOneRequestPerFullBatch() {
    // given
    whenStockCardsAreRequested().thenReturn(pageOf(randomCard()));

    // when
    service.findByIds(randomIds(MAX_IDS_PER_REQUEST * 2));

    // then
    List<URI> uris = captureRequests(2);
    List<UUID> requestedIds = new ArrayList<>();
    assertBatch(uris.get(0), MAX_IDS_PER_REQUEST, requestedIds);
    assertBatch(uris.get(1), MAX_IDS_PER_REQUEST, requestedIds);
  }

  @Test
  public void shouldKeepFullBatchRequestUnderNginxLimit() {
    // given
    whenStockCardsAreRequested().thenReturn(pageOf(randomCard()));

    // when
    service.findByIds(randomIds(MAX_IDS_PER_REQUEST));

    // then
    URI uri = captureRequests(1).get(0);
    String requestLine = "GET " + uri.getRawPath() + "?" + uri.getRawQuery() + " HTTP/1.1";
    assertThat(requestLine.length(), is(lessThan(MAX_REQUEST_LINE_LENGTH)));
  }

  private Set<UUID> randomIds(int count) {
    return IntStream.range(0, count)
        .mapToObj(i -> UUID.randomUUID())
        .collect(Collectors.toSet());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private OngoingStubbing<ResponseEntity> whenStockCardsAreRequested() {
    return when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
        any(ParameterizedTypeReference.class)));
  }

  @SuppressWarnings("unchecked")
  private List<URI> captureRequests(int count) {
    verify(restTemplate, times(count)).exchange(uriCaptor.capture(), eq(HttpMethod.GET),
        any(HttpEntity.class), any(ParameterizedTypeReference.class));
    return uriCaptor.getAllValues();
  }

  private void assertBatch(URI uri, int batchSize, List<UUID> requestedIds) {
    MultiValueMap<String, String> params = UriComponentsBuilder.fromUri(uri).build()
        .getQueryParams();

    assertThat(params.get("id").size(), is(batchSize));
    assertThat(params.getFirst("size"), is(String.valueOf(batchSize)));
    params.get("id").forEach(id -> requestedIds.add(UUID.fromString(id)));
  }

  private StockCardDto randomCard() {
    return StockCardDto.builder().id(UUID.randomUUID()).build();
  }

  private ResponseEntity<PageImplRepresentation<StockCardDto>> pageOf(StockCardDto card) {
    PageImplRepresentation<StockCardDto> page = new PageImplRepresentation<>();
    page.setContent(Collections.singletonList(card));
    return new ResponseEntity<>(page, HttpStatus.OK);
  }
}
