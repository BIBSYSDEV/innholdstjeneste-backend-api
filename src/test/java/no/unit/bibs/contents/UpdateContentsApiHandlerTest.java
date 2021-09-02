package no.unit.bibs.contents;

import static no.unit.bibs.contents.CreateContentsApiHandlerTest.TEST_ISBN;
import static no.unit.bibs.contents.DynamoDBClientTest.SAMPLE_TERM;
import static nva.commons.apigateway.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;
import static nva.commons.core.JsonUtils.objectMapper;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.file.Path;
import no.unit.bibs.contents.exception.ParameterException;
import no.unit.nva.testutils.IoUtils;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.Environment;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdateContentsApiHandlerTest {

    private Environment environment;
    private Table dynamoTable;
    private DynamoDBClient dynamoDBClient;
    private S3Client s3Client;
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
        s3Client = mock(S3Client.class);
        handler = new UpdateContentsApiHandler(environment, dynamoDBClient, s3Client);
    }

    @Test
    public void processInputTest() throws ApiGatewayException, JsonProcessingException {
        DynamoDBClient dynamoDbclient = mock(DynamoDBClient.class);
        S3Client s3Client = mock(S3Client.class);
        Environment environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        UpdateContentsApiHandler handler = new UpdateContentsApiHandler(environment, dynamoDbclient, s3Client);
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        ContentsDocument contentsDocument = objectMapper.readValue(contents, ContentsDocument.class);
        ContentsRequest request = new ContentsRequest(contentsDocument);
        when(dynamoDbclient.updateContents(contentsDocument)).thenReturn(contents);
        GatewayResponse gatewayResponse = handler.processInput(request, new RequestInfo(), mock(Context.class));
        assertEquals(HttpStatus.SC_CREATED, gatewayResponse.getStatusCode());
    }

    @Test
    public void testEmptyIsbnInContentsDocument() throws ApiGatewayException, JsonProcessingException {
        DynamoDBClient client = mock(DynamoDBClient.class);
        S3Client s3Client = mock(S3Client.class);
        Environment environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        UpdateContentsApiHandler handler = new UpdateContentsApiHandler(environment, client, s3Client);
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        contents = contents.replace(TEST_ISBN, EMPTY_STRING);
        ContentsDocument contentsDocument = objectMapper.readValue(contents, ContentsDocument.class);
        ContentsRequest request = new ContentsRequest(contentsDocument);
        when(client.updateContents(contentsDocument)).thenReturn(contents);
        GatewayResponse gatewayResponse = handler.processInput(request, new RequestInfo(), mock(Context.class));
        assertEquals(HttpStatus.SC_BAD_REQUEST, gatewayResponse.getStatusCode());
    }

    @Test
    public void testEmptyContentsDocumentRequest() throws ApiGatewayException, JsonProcessingException {
        DynamoDBClient client = mock(DynamoDBClient.class);
        S3Client s3Client = mock(S3Client.class);
        Environment environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        UpdateContentsApiHandler handler = new UpdateContentsApiHandler(environment, client, s3Client);
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        ContentsDocument contentsDocument = objectMapper.readValue(contents, ContentsDocument.class);
        ContentsRequest request = new ContentsRequest(contentsDocument);
        when(client.getContents(anyString())).thenReturn(contents);
        GatewayResponse gatewayResponse = handler.processInput(request, new RequestInfo(), mock(Context.class));
        assertEquals(HttpStatus.SC_OK, gatewayResponse.getStatusCode());
    }

    @Test
    public void testGetContentsNotFoundWithFinalCrashing() throws ApiGatewayException, JsonProcessingException {
        DynamoDBClient client = mock(DynamoDBClient.class);
        S3Client s3Client = mock(S3Client.class);
        Environment environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        UpdateContentsApiHandler handler = new UpdateContentsApiHandler(environment, client, s3Client);
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        ContentsDocument contentsDocument = objectMapper.readValue(contents, ContentsDocument.class);
        ContentsRequest request = new ContentsRequest(contentsDocument);
        when(client.getContents(anyString())).thenThrow(NotFoundException.class);
        GatewayResponse gatewayResponse = handler.processInput(request, new RequestInfo(), mock(Context.class));
        assertEquals(HttpStatus.SC_BAD_REQUEST, gatewayResponse.getStatusCode());
    }

    @Test
    public void testGetContentsNotFound() throws ApiGatewayException, JsonProcessingException {
        DynamoDBClient client = mock(DynamoDBClient.class);
        S3Client s3Client = mock(S3Client.class);
        Environment environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        UpdateContentsApiHandler handler = new UpdateContentsApiHandler(environment, client, s3Client);
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        ContentsDocument contentsDocument = objectMapper.readValue(contents, ContentsDocument.class);
        ContentsRequest request = new ContentsRequest(contentsDocument);
        when(client.getContents(anyString())).thenThrow(NotFoundException.class).thenReturn(contents);
        GatewayResponse gatewayResponse = handler.processInput(request, new RequestInfo(), mock(Context.class));
        assertEquals(HttpStatus.SC_CREATED, gatewayResponse.getStatusCode());
    }

    @Test
    public void testGetContentsNotFoundThenCrashing() throws ApiGatewayException, JsonProcessingException {
        DynamoDBClient client = mock(DynamoDBClient.class);
        S3Client s3Client = mock(S3Client.class);
        Environment environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        UpdateContentsApiHandler handler = new UpdateContentsApiHandler(environment, client, s3Client);
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        ContentsDocument contentsDocument = objectMapper.readValue(contents, ContentsDocument.class);
        ContentsRequest request = new ContentsRequest(contentsDocument);
        when(client.getContents(anyString())).thenThrow(IllegalArgumentException.class);
        GatewayResponse gatewayResponse = handler.processInput(request, new RequestInfo(), mock(Context.class));
        assertEquals(HttpStatus.SC_CONFLICT, gatewayResponse.getStatusCode());
    }


    @Test
    void getSuccessStatusCodeReturnsOK() {
        UpdateContentsApiHandler handler = new UpdateContentsApiHandler(environment, dynamoDBClient, s3Client);
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