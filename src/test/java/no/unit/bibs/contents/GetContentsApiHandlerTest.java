package no.unit.bibs.contents;

import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.exceptions.commonexceptions.NotFoundException;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.IoUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetContentsApiHandlerTest {

    public static final String SAMPLE_SEARCH_TERM = "searchTerm";
    public static final String ERROR = "error";
    private Environment environment;
    private GetContentsApiHandler getContentsApiHandler;
    private Table dynamoTable;

    private void initEnvironment() {
        environment = mock(Environment.class);
        dynamoTable = mock(Table.class);
    }

    @BeforeEach
    public void init() {
        initEnvironment();
        getContentsApiHandler = new GetContentsApiHandler(environment, new DynamoDBClient(dynamoTable));
    }

    @Test
    void getSuccessStatusCodeReturnsOK() {
        GetContentsApiHandler getContentsApiHandler =
                new GetContentsApiHandler(environment, new DynamoDBClient(dynamoTable));
        GatewayResponse response =  new GatewayResponse(environment, SAMPLE_SEARCH_TERM, HttpStatus.SC_OK);
        Integer statusCode = getContentsApiHandler.getSuccessStatusCode(null, response);
        assertEquals(statusCode, HttpStatus.SC_OK);
    }

    @Test
    void handlerReturnsContentsDocumentByGivenTerm() throws ApiGatewayException {
        DynamoDBClient dynamoDBClient = mock(DynamoDBClient.class);
        var handler = new GetContentsApiHandler(environment, dynamoDBClient);
        String contents = IoUtils.stringFromResources(Path.of(DynamoDBClientTest.GET_CONTENTS_JSON));
        var expected = new GatewayResponse(environment, contents, HttpStatus.SC_OK);
        when(dynamoDBClient.getContents(SAMPLE_SEARCH_TERM)).thenReturn(contents);
        var actual = handler.processInput(null, getRequestInfo(), mock(Context.class));
        assertEquals(expected.getBody(), actual.getBody());
    }

    @Test
    void handlerReturnsNotFoundExceptionWhenGivenMissingIsbn() throws ApiGatewayException {
        DynamoDBClient dynamoDBClient = mock(DynamoDBClient.class);
        var handler = new GetContentsApiHandler(environment, dynamoDBClient);
        String contents = IoUtils.stringFromResources(Path.of(DynamoDBClientTest.GET_CONTENTS_JSON));
        when(dynamoDBClient.getContents(SAMPLE_SEARCH_TERM)).thenThrow(new NotFoundException(SAMPLE_SEARCH_TERM));
        var actual = handler.processInput(null, getRequestInfo(), mock(Context.class));
        assertTrue(actual.getBody().contains(SAMPLE_SEARCH_TERM));
        assertTrue(actual.getBody().contains(ERROR));
    }


    private RequestInfo getRequestInfo() {
        var requestInfo = new RequestInfo();
        requestInfo.setQueryParameters(Map.of(GetContentsApiHandler.ISBN, SAMPLE_SEARCH_TERM));
        return requestInfo;
    }

}
