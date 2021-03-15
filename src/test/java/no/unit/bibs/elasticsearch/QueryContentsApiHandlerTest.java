package no.unit.bibs.elasticsearch;

import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.unit.bibs.elasticsearch.exception.CommunicationException;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.IoUtils;
import nva.commons.utils.JsonUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QueryContentsApiHandlerTest {

    public static final String SAMPLE_SEARCH_TERM = "searchTerm";
    public static final ObjectMapper mapper = JsonUtils.objectMapper;
    public static final String ROUNDTRIP_RESPONSE_JSON = "roundtripResponse.json";
    public static final URI EXAMPLE_CONTEXT = URI.create("https://example.org/search");
    public static final List<JsonNode> SAMPLE_HITS = Collections.EMPTY_LIST;
    public static final int SAMPLE_TOOK = 0;
    public static final int SAMPLE_TOTAL = 0;
    private Environment environment;
    private QueryContentsApiHandler queryContentsApiHandler;
    private Table dynamoTable;

    private void initEnvironment() {
        environment = mock(Environment.class);
        dynamoTable = mock(Table.class);
    }

    @BeforeEach
    public void init() {
        initEnvironment();
        queryContentsApiHandler = new QueryContentsApiHandler(environment, new DynamoDBClient(dynamoTable));
    }

    @Test
    void defaultConstructorThrowsIllegalStateExceptionWhenEnvironmentNotDefined() {
        assertThrows(CommunicationException.class, QueryContentsApiHandler::new);
    }

    @Test
    void getSuccessStatusCodeReturnsOK() {
        QueryContentsResponse response =  new QueryContentsResponse(EXAMPLE_CONTEXT,
                SAMPLE_TOOK,
                SAMPLE_TOTAL,
                SAMPLE_HITS);
        Integer statusCode = queryContentsApiHandler.getSuccessStatusCode(null, response);
        assertEquals(statusCode, HttpStatus.SC_OK);
    }

    @Test
    void handlerReturnsContentsDocumentByGivenTerm() throws ApiGatewayException, IOException {
        DynamoDBClient dynamoDBClient = mock(DynamoDBClient.class);
        var handler = new QueryContentsApiHandler(environment, dynamoDBClient);
        var expected = mapper.readValue(IoUtils.stringFromResources(Path.of(ROUNDTRIP_RESPONSE_JSON)),
                QueryContentsResponse.class);
        when(dynamoDBClient.get(SAMPLE_SEARCH_TERM)).thenReturn(expected);
        var actual = handler.processInput(null, getRequestInfo(), mock(Context.class));
        assertEquals(expected, actual);
    }

    @Test
    void handlerThrowsExceptionWhenGatewayIsBad() throws IOException {
        var dynamoDBClient = new DynamoDBClient(dynamoTable);
        var handler = new QueryContentsApiHandler(environment, dynamoDBClient);
        Executable executable = () -> handler.processInput(null, getRequestInfo(), mock(Context.class));
        assertThrows(ApiGatewayException.class, executable);
    }

    private RequestInfo getRequestInfo() {
        var requestInfo = new RequestInfo();
        requestInfo.setQueryParameters(Map.of(QueryContentsApiHandler.ISBN, SAMPLE_SEARCH_TERM));
        return requestInfo;
    }

}
