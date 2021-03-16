package no.unit.bibs.contents;

import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.unit.bibs.contents.exception.CommunicationException;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.IoUtils;
import nva.commons.utils.JsonUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetContentsApiHandlerTest {

    public static final String SAMPLE_SEARCH_TERM = "searchTerm";
    public static final ObjectMapper mapper = JsonUtils.objectMapper;
    public static final String ROUNDTRIP_RESPONSE_JSON = "roundtripResponse.json";
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
    void defaultConstructorThrowsIllegalStateExceptionWhenEnvironmentNotDefined() {
        assertThrows(CommunicationException.class, GetContentsApiHandler::new);
    }

    @Test
    void getSuccessStatusCodeReturnsOK() {
        GatewayResponse response =  new GatewayResponse(environment, SAMPLE_SEARCH_TERM, HttpStatus.SC_OK);
        Integer statusCode = getContentsApiHandler.getSuccessStatusCode(null, response);
        assertEquals(statusCode, HttpStatus.SC_OK);
    }

    @Test
    void handlerReturnsContentsDocumentByGivenTerm() throws ApiGatewayException, IOException {
        DynamoDBClient dynamoDBClient = mock(DynamoDBClient.class);
        var handler = new GetContentsApiHandler(environment, dynamoDBClient);
        String contents = IoUtils.stringFromResources(Path.of(DynamoDBClientTest.GET_CONTENTS_JSON));
        var expected = new GatewayResponse(environment, contents, HttpStatus.SC_OK);
        when(dynamoDBClient.getContents(SAMPLE_SEARCH_TERM)).thenReturn(contents);
        var actual = handler.processInput(null, getRequestInfo(), mock(Context.class));
        assertEquals(expected.getBody(), actual.getBody());
    }


    private RequestInfo getRequestInfo() {
        var requestInfo = new RequestInfo();
        requestInfo.setQueryParameters(Map.of(GetContentsApiHandler.ISBN, SAMPLE_SEARCH_TERM));
        return requestInfo;
    }

}
