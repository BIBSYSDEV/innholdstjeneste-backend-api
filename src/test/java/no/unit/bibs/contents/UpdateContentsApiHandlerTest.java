package no.unit.bibs.contents;

import static no.unit.bibs.contents.CreateContentsApiHandlerTest.TEST_ISBN;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.apigateway.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.net.HttpURLConnection;
import java.nio.file.Path;

import no.unit.bibs.contents.exception.ParameterException;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.ConflictException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdateContentsApiHandlerTest {

    private Environment environment;
    private DynamoDBClient dynamoDBClient;
    private StorageClient storageClient;
    private UpdateContentsApiHandler handler;

    public static final String CREATE_CONTENTS_EVENT = "createContentsEvent.json";

    /**
     * javadoc for checkstyle.
     */
    @BeforeEach
    public void init() {
        environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        dynamoDBClient = mock(DynamoDBClient.class);
        storageClient = mock(StorageClient.class);
        handler = new UpdateContentsApiHandler(environment, dynamoDBClient, storageClient);
    }

    @Test
    public void processInputTest() throws ApiGatewayException, JsonProcessingException {
        DynamoDBClient dynamoDbclient = mock(DynamoDBClient.class);
        StorageClient storageClient = mock(StorageClient.class);
        Environment environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        UpdateContentsApiHandler handler = new UpdateContentsApiHandler(environment, dynamoDbclient, storageClient);
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        ContentsDocument contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        ContentsRequest request = new ContentsRequest(contentsDocument);
        when(dynamoDbclient.getContents(anyString())).thenReturn(contents);
        var actual = handler.processInput(request, new RequestInfo(), mock(Context.class));
        assertEquals(contentsDocument, actual);
    }

    @Test
    public void testEmptyIsbnInContentsDocument() throws JsonProcessingException {
        DynamoDBClient client = mock(DynamoDBClient.class);
        StorageClient storageClient = mock(StorageClient.class);
        Environment environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        UpdateContentsApiHandler handler = new UpdateContentsApiHandler(environment, client, storageClient);
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        contents = contents.replace(TEST_ISBN, EMPTY_STRING);
        ContentsDocument contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        ContentsRequest request = new ContentsRequest(contentsDocument);
        Exception exception = assertThrows(BadRequestException.class, () -> {
            handler.processInput(request, new RequestInfo(), mock(Context.class));
        });
    }

    @Test
    public void testGetContentsNotFoundWithFinalCrashing() throws ApiGatewayException, JsonProcessingException {
        DynamoDBClient client = mock(DynamoDBClient.class);
        StorageClient storageClient = mock(StorageClient.class);
        Environment environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        UpdateContentsApiHandler handler = new UpdateContentsApiHandler(environment, client, storageClient);
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        ContentsDocument contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        ContentsRequest request = new ContentsRequest(contentsDocument);
        when(client.getContents(anyString())).thenThrow(NotFoundException.class);
        Exception exception = assertThrows(NotFoundException.class, () -> {
            handler.processInput(request, new RequestInfo(), mock(Context.class));
        });
    }

    @Test
    public void testGetContentsNotFound() throws ApiGatewayException, JsonProcessingException {
        DynamoDBClient client = mock(DynamoDBClient.class);
        StorageClient storageClient = mock(StorageClient.class);
        Environment environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        UpdateContentsApiHandler handler = new UpdateContentsApiHandler(environment, client, storageClient);
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        ContentsDocument contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        ContentsRequest request = new ContentsRequest(contentsDocument);
        when(client.getContents(anyString())).thenThrow(NotFoundException.class).thenReturn(contents);
        var actual = handler.processInput(request, new RequestInfo(), mock(Context.class));
        assertEquals(contentsDocument, actual);
    }

    @Test
    public void testGetContentsNotFoundThenCrashing() throws ApiGatewayException, JsonProcessingException {
        DynamoDBClient client = mock(DynamoDBClient.class);
        StorageClient storageClient = mock(StorageClient.class);
        Environment environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        UpdateContentsApiHandler handler = new UpdateContentsApiHandler(environment, client, storageClient);
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        ContentsDocument contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        ContentsRequest request = new ContentsRequest(contentsDocument);
        when(client.getContents(anyString())).thenThrow(IllegalArgumentException.class);
        Exception exception = assertThrows(ConflictException.class, () -> {
            handler.processInput(request, new RequestInfo(), mock(Context.class));
        });
    }


    @Test
    void getSuccessStatusCodeReturnsOK() {
        UpdateContentsApiHandler handler = new UpdateContentsApiHandler(environment, dynamoDBClient, storageClient);
        Integer statusCode = handler.getSuccessStatusCode(null, null);
        assertEquals(statusCode, HttpURLConnection.HTTP_CREATED);
    }


    @Test
    void handlerThrowsExceptionWithEmptyRequest()  {
        Exception exception = assertThrows(ParameterException.class, () -> {
            handler.processInput(null, new RequestInfo(), mock(Context.class));
        });
        assertTrue(exception.getMessage().contains(UpdateContentsApiHandler.NO_PARAMETERS_GIVEN_TO_HANDLER));
    }

}