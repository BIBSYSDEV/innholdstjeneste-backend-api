package no.unit.bibs.contents;

import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.bibs.contents.exception.ParameterException;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.IoUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static no.unit.bibs.contents.DynamoDBClientTest.SAMPLE_TERM;
import static nva.commons.handlers.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;
import static nva.commons.utils.JsonUtils.objectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateContentsApiHandlerTest {

    private Environment environment;
    private Table dynamoTable;
    private DynamoDBClient dynamoDBClient;
    private UpdateContentsApiHandler handler;

    public static final String CREATE_CONTENTS_EVENT = "createContentsEvent.json";

    /**
     * javadoc for checkstyle.
     */
    @BeforeEach
    public void init() {
        environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        dynamoTable = mock(Table.class);
        dynamoDBClient = mock(DynamoDBClient.class);
        handler = new UpdateContentsApiHandler(environment, dynamoDBClient);
    }

    @Test
    public void processInputTest() throws ApiGatewayException, JsonProcessingException {
        DynamoDBClient client = mock(DynamoDBClient.class);
        Environment environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        UpdateContentsApiHandler handler = new UpdateContentsApiHandler(environment, client);
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        ContentsDocument contentsDocument = objectMapper.readValue(contents, ContentsDocument.class);
        ContentsRequest request = new ContentsRequest(contentsDocument);
        when(client.updateContents(contentsDocument)).thenReturn(contents);
        GatewayResponse gatewayResponse = handler.processInput(request, new RequestInfo(), mock(Context.class));
        assertEquals(HttpStatus.SC_CREATED, gatewayResponse.getStatusCode());
    }


    @Test
    void getSuccessStatusCodeReturnsOK() {
        UpdateContentsApiHandler handler = new UpdateContentsApiHandler(environment);
        GatewayResponse response =  new GatewayResponse(environment, SAMPLE_TERM, HttpStatus.SC_OK);
        Integer statusCode = handler.getSuccessStatusCode(null, response);
        assertEquals(statusCode, HttpStatus.SC_OK);
    }


    @Test
    void handlerThrowsExceptionWithEmptyRequest()  {
        Exception exception = assertThrows(ParameterException.class, () -> {
            handler.processInput(null, new RequestInfo(), mock(Context.class));
        });

        assertTrue(exception.getMessage().contains(UpdateContentsApiHandler.NO_PARAMETERS_GIVEN_TO_HANDLER));
    }

}