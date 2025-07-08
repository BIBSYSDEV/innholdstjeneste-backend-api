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
    private GetContentsApiHandler contentsApiHandler;
    private DynamoDbClient client;
    private RequestInfo requestInfo;


    @BeforeEach
    public void init() {
        environment = mock(Environment.class);
        requestInfo = mock(RequestInfo.class);
        when(requestInfo.getQueryParameters()).thenReturn(Map.of(GetContentsApiHandler.ISBN, SAMPLE_SEARCH_TERM));
        when(environment.readEnv(GetContentsApiHandler.ALLOWED_ORIGIN_ENV)).thenReturn("*");
        when(environment.readEnv("DYNAMODB_TABLE_NAME_ENV"))
            .thenReturn("TEST_TABLE_NAME");

        client = mock(DynamoDbClient.class);
        contentsApiHandler = new GetContentsApiHandler(environment, new DBClient(client));
    }

    @Test
    void getSuccessStatusCodeReturnsOK() {
        var statusCode = contentsApiHandler.getSuccessStatusCode(null, null);
        assertEquals(statusCode, HttpURLConnection.HTTP_OK);
    }

    @Test
    void handlerReturnsContentsDocumentByGivenTerm() throws ApiGatewayException, JsonProcessingException {
        var dynamoDBClient = mock(DBClient.class);
        var handler = new GetContentsApiHandler(environment, dynamoDBClient);
        var  contents = IoUtils.stringFromResources(Path.of(DBClientTest.GET_CONTENTS_JSON));
        var contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        when(dynamoDBClient.getContents(SAMPLE_SEARCH_TERM)).thenReturn(contents);
        var actual = handler.processInput(null, requestInfo, mock(Context.class));
        assertEquals(contentsDocument, actual);
    }

    @Test
    void handlerReturnsBadRequestExceptionWhenMissingIsbn() {
        var dynamoDBClient = mock(DBClient.class);
        var handler = new GetContentsApiHandler(environment, dynamoDBClient);
        Exception exception = assertThrows(BadRequestException.class, () -> {
            handler.processInput(null, requestInfo, mock(Context.class));
        });
        assertTrue(exception.getMessage().contains(GetContentsApiHandler.ISBN));
    }


}
