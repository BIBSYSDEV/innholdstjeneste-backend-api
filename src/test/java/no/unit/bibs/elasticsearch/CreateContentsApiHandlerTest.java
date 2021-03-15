package no.unit.bibs.elasticsearch;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.GatewayResponse;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.IoUtils;
import org.apache.http.HttpStatus;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;

import static no.unit.bibs.elasticsearch.DynamoDBClient.*;
import static nva.commons.handlers.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;
import static nva.commons.utils.JsonUtils.objectMapper;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateContentsApiHandlerTest {

    public static final String MESSAGE = "message";
    public static final Instant TIMESTAMP = Instant.now();
    public static final String SAMPLE_ELASTICSEARCH_RESPONSE_JSON = "sample_elasticsearch_response.json";
    public static final String CREATE_CONTENTS_EVENT = "createContentsEvent.json";
    private Environment environment;
    private CreateContentsApiHandler handler;
    private Context context;

    private void initEnvironment() {
        environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
    }

    @BeforeEach
    public void init() {
        initEnvironment();
        handler = new CreateContentsApiHandler(environment);
        this.context = mock(Context.class);
    }

    @Test
    void defaultConstructorThrowsIllegalStateExceptionWhenEnvironmentNotDefined() {
        assertThrows(IllegalStateException.class, QueryContentsApiHandler::new);
    }

    @Test
    void getSuccessStatusCodeReturnsOK() {
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        CreateContentsRequest request = new CreateContentsRequest(contents);
        CreateContentsResponse response =  new CreateContentsResponse(MESSAGE, request, HttpStatus.SC_OK, TIMESTAMP);
        Integer statusCode = handler.getSuccessStatusCode(null, response);
        assertEquals(statusCode, HttpStatus.SC_OK);
    }

    @Test
    void handlerReturnsSearchResultsWhenQueryIsSingleTerm() throws ApiGatewayException, IOException {
        var elasticSearchClient = new DynamoDBClient(environment, setUpRestHighLevelClient());
        var handler = new CreateContentsApiHandler(environment, elasticSearchClient);
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        CreateContentsRequest request = new CreateContentsRequest(contents);
        var actual = handler.processInput(request, new RequestInfo(), mock(Context.class));
        assertEquals(contents, actual.getRequest().getContents());
        assertEquals(CreateContentsApiHandler.CHECK_LOG_FOR_DETAILS_MESSAGE, actual.getMessage());
    }

    @Test
    void handlerThrowsExceptionWhenGatewayIsBad() throws IOException {
        var elasticSearchClient = new DynamoDBClient(environment, setUpBadGateWay());
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

    @Test
    public void handleRequestReturnsCreatedIfContentsIsPosted() throws IOException {
        CreateContentsRequest request = readMockCreateContentsRequestFromJsonFile();
        GatewayResponse<Void> response = sendRequest(request);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.SC_CREATED)));
    }

    private <T> GatewayResponse<T> sendRequest(CreateContentsRequest request) throws IOException {
        InputStream input = createRequest(request);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.handleRequest(input, output, context);

        return GatewayResponse.fromOutputStream(output);
    }

    private InputStream createRequest(CreateContentsRequest request) throws JsonProcessingException {
        return new HandlerRequestBuilder<CreateContentsRequest>(objectMapper)
                .withBody(request)
                .build();
    }

    private CreateContentsRequest readMockCreateContentsRequestFromJsonFile() {
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        return new CreateContentsRequest(contents);
    }
}
