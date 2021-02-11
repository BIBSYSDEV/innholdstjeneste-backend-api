package no.unit.bibs.elasticsearch;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.IoUtils;
import nva.commons.utils.JsonUtils;
import org.apache.http.HttpStatus;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;

import static no.unit.bibs.elasticsearch.ElasticSearchHighLevelRestClient.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateContentsApiHandlerTest {

    public static final String MESSAGE = "message";
    public static final Instant TIMESTAMP = Instant.now();
    public static final String SAMPLE_ELASTICSEARCH_RESPONSE_JSON = "sample_elasticsearch_response.json";
    public static final ObjectMapper mapper = JsonUtils.objectMapper;
    public static final String ROUNDTRIP_RESPONSE_JSON = "roundtripResponse.json";
    public static final String PUT_REQUEST = "createContentsEvent.json";
    private Environment environment;
    private CreateContentsApiHandler puContentsApiHandler;

    private void initEnvironment() {
        environment = mock(Environment.class);
        when(environment.readEnv(ELASTICSEARCH_ENDPOINT_ADDRESS_KEY)).thenReturn("localhost");
        when(environment.readEnv(ELASTICSEARCH_ENDPOINT_INDEX_KEY)).thenReturn("resources");
        when(environment.readEnv(ELASTICSEARCH_ENDPOINT_API_SCHEME_KEY)).thenReturn("http");
    }

    @BeforeEach
    public void init() {
        initEnvironment();
        puContentsApiHandler = new CreateContentsApiHandler(environment);
    }

    @Test
    void defaultConstructorThrowsIllegalStateExceptionWhenEnvironmentNotDefined() {
        assertThrows(IllegalStateException.class, QueryContentsApiHandler::new);
    }

    @Test
    void getSuccessStatusCodeReturnsOK() {
        String contents = IoUtils.stringFromResources(Path.of(PUT_REQUEST));
        CreateContentsRequest request = new CreateContentsRequest(contents);
        CreateContentsResponse response =  new CreateContentsResponse(MESSAGE,
        request, HttpStatus.SC_OK, TIMESTAMP);
        Integer statusCode = puContentsApiHandler.getSuccessStatusCode(null, response);
        assertEquals(statusCode, HttpStatus.SC_OK);
    }

    @Test
    void handlerReturnsSearchResultsWhemQueryIsSingleTerm() throws ApiGatewayException, IOException {
        var elasticSearchClient = new ElasticSearchHighLevelRestClient(environment, setUpRestHighLevelClient());
        var handler = new CreateContentsApiHandler(environment, elasticSearchClient);
        String contents = IoUtils.stringFromResources(Path.of(PUT_REQUEST));
        CreateContentsRequest request = new CreateContentsRequest(contents);
        var actual = handler.processInput(request, new RequestInfo(), mock(Context.class));
        assertEquals(contents, actual.getRequest().getContents());
        assertEquals(CreateContentsApiHandler.CHECK_LOG_FOR_DETAILS_MESSAGE, actual.getMessage());
    }

    @Test
    void handlerThrowsExceptionWhenGatewayIsBad() throws IOException {
        var elasticSearchClient = new ElasticSearchHighLevelRestClient(environment, setUpBadGateWay());
        var handler = new CreateContentsApiHandler(environment, elasticSearchClient);
        Executable executable = () -> handler.processInput(null, new RequestInfo(), mock(Context.class));
        assertThrows(ApiGatewayException.class, executable);
    }

    private RestHighLevelClient setUpRestHighLevelClient() throws IOException {
        String result = IoUtils.stringFromResources(Path.of(SAMPLE_ELASTICSEARCH_RESPONSE_JSON));
        SearchResponse searchResponse = getSearchResponse(result);
        RestHighLevelClient restHighLevelClient = mock(RestHighLevelClient.class);
        when(restHighLevelClient.search(any(), any())).thenReturn(searchResponse);
        return restHighLevelClient;
    }

    private RestHighLevelClient setUpBadGateWay() throws IOException {
        RestHighLevelClient restHighLevelClient = mock(RestHighLevelClient.class);
        when(restHighLevelClient.search(any(), any())).thenThrow(IOException.class);
        return restHighLevelClient;
    }

    private SearchResponse getSearchResponse(String hits) {
        var searchResponse = mock(SearchResponse.class);
        when(searchResponse.toString()).thenReturn(hits);
        return searchResponse;
    }
}
