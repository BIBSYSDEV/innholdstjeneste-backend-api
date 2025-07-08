package no.unit.bibs.contents;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.bibs.contents.exception.ParameterException;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.ConflictException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.util.Map;

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

class UpdateContentsApiHandlerTest {

    private RequestInfo requestInfo;
    private DBClient dbClient;
    private UpdateContentsApiHandler handler;

    public static final String CREATE_CONTENTS_EVENT = "createContentsEvent.json";

    /**
     * javadoc for checkstyle.
     */
    @BeforeEach
    public void init() {
        var environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        dbClient = mock(DBClient.class);
        var storageClient = mock(StorageClient.class);
        handler = new UpdateContentsApiHandler(environment, dbClient, storageClient);
        requestInfo = mock(RequestInfo.class);
        when(requestInfo.getQueryParameters()).thenReturn(Map.of("isbn", TEST_ISBN));
    }

    @Test
    public void processInputTest() throws ApiGatewayException, JsonProcessingException {


        var contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        var contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        var request = new ContentsRequest(contentsDocument);
        when(dbClient.getContents(anyString())).thenReturn(contents);
        var actual = handler.processInput(request, requestInfo, mock(Context.class));
        assertEquals(contentsDocument, actual);
    }

    @Test
    public void testEmptyIsbnInContentsDocument() throws JsonProcessingException {

        var contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        contents = contents.replace(TEST_ISBN, EMPTY_STRING);
        var contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        var request = new ContentsRequest(contentsDocument);
        assertThrows(BadRequestException.class, () ->
            handler.processInput(request, requestInfo, mock(Context.class))
        );
    }

    @Test
    public void testGetContentsNotFoundWithFinalCrashing() throws ApiGatewayException, JsonProcessingException {

        var contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        var contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        var request = new ContentsRequest(contentsDocument);
        when(dbClient.getContents(anyString())).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () ->
            handler.processInput(request, requestInfo, mock(Context.class))
        );
    }

    @Test
    public void testGetContentsNotFound() throws ApiGatewayException, JsonProcessingException {

        var contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        var contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        var request = new ContentsRequest(contentsDocument);
        when(dbClient.getContents(anyString())).thenThrow(NotFoundException.class).thenReturn(contents);
        var actual = handler.processInput(request, requestInfo, mock(Context.class));
        assertEquals(contentsDocument, actual);
    }

    @Test
    public void testGetContentsNotFoundThenCrashing() throws ApiGatewayException, JsonProcessingException {

        var contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        var contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        var request = new ContentsRequest(contentsDocument);
        when(dbClient.getContents(anyString())).thenThrow(IllegalArgumentException.class);
        assertThrows(ConflictException.class, () ->
            handler.processInput(request, requestInfo, mock(Context.class))
        );
    }


    @Test
    void getSuccessStatusCodeReturnsOK() {
        var statusCode = handler.getSuccessStatusCode(null, null);
        assertEquals(HttpURLConnection.HTTP_CREATED, statusCode);
    }


    @Test
    void handlerThrowsExceptionWithEmptyRequest() {
        var exception = assertThrows(ParameterException.class, () ->
            handler.processInput(null, requestInfo, mock(Context.class))
        );
        assertTrue(exception.getMessage().contains(UpdateContentsApiHandler.NO_PARAMETERS_GIVEN_TO_HANDLER));
    }

}