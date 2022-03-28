package no.unit.bibs.contents;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;

import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class GetContentsApiHandlerTest {

    public static final String SAMPLE_SEARCH_TERM = "searchTerm";
    private Environment environment;
    private GetContentsApiHandler getContentsApiHandler;
    private DynamoDbClient client;

    private void initEnvironment() {
        environment = mock(Environment.class);
        client = mock(DynamoDbClient.class);
    }

    @BeforeEach
    public void init() {
        initEnvironment();
        getContentsApiHandler = new GetContentsApiHandler(environment, new DynamoDBClient(client));
    }

    @Test
    void getSuccessStatusCodeReturnsOK() {
        Integer statusCode = getContentsApiHandler.getSuccessStatusCode(null, null);
        assertEquals(statusCode, HttpURLConnection.HTTP_OK);
    }

    @Test
    void handlerReturnsContentsDocumentByGivenTerm() throws ApiGatewayException, JsonProcessingException {
        DynamoDBClient dynamoDBClient = mock(DynamoDBClient.class);
        var handler = new GetContentsApiHandler(environment, dynamoDBClient);
        String contents = IoUtils.stringFromResources(Path.of(DynamoDBClientTest.GET_CONTENTS_JSON));
        ContentsDocument contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        when(dynamoDBClient.getContents(SAMPLE_SEARCH_TERM)).thenReturn(contents);
        var actual = handler.processInput(null, getRequestInfo(), mock(Context.class));
        assertEquals(contentsDocument, actual);
    }

    @Test
    void handlerReturnsBadRequestExceptionWhenMissingIsbn() {
        DynamoDBClient dynamoDBClient = mock(DynamoDBClient.class);
        var handler = new GetContentsApiHandler(environment, dynamoDBClient);
        Exception exception = assertThrows(BadRequestException.class, () -> {
            handler.processInput(null, new RequestInfo(), mock(Context.class));
        });
        assertTrue(exception.getMessage().contains(GetContentsApiHandler.ISBN));
    }


    private RequestInfo getRequestInfo() {
        var requestInfo = new RequestInfo();
        requestInfo.setQueryParameters(Map.of(GetContentsApiHandler.ISBN, SAMPLE_SEARCH_TERM));
        return requestInfo;
    }

}
