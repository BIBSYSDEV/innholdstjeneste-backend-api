package no.unit.bibs.contents;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.util.Map;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetContentsApiHandlerTest {

    public static final String SAMPLE_SEARCH_TERM = "searchTerm";
    private RequestInfo requestInfo;
    private DBClient dbClient;
    private CreateContentsApiHandler mockedApiHandler;


    @BeforeEach
    public void init() {
        requestInfo = mock(RequestInfo.class);
        when(requestInfo.getQueryParameters()).thenReturn(Map.of(GetContentsApiHandler.ISBN, SAMPLE_SEARCH_TERM));

        Environment environment = mock(Environment.class);
        when(environment.readEnv(GetContentsApiHandler.ALLOWED_ORIGIN_ENV)).thenReturn("*");
        when(environment.readEnv("COGNITO_AUTHORIZER_URLS"))
            .thenReturn("https://test.cognito.auth.url");

        dbClient = mock(DBClient.class);
        var storageClient = mock(StorageClient.class);
        mockedApiHandler = new CreateContentsApiHandler(environment, dbClient, storageClient);

    }

    @Test
    void getSuccessStatusCodeReturnsOK() {
        var statusCode = mockedApiHandler.getSuccessStatusCode(null, null);
        assertEquals(HttpURLConnection.HTTP_CREATED, statusCode);
    }

    @Test
    void handlerReturnsContentsDocumentByGivenTerm() throws ApiGatewayException, JsonProcessingException {
        var contents = IoUtils.stringFromResources(Path.of(DBClientTest.GET_CONTENTS_JSON));
        var contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        when(dbClient.getContents(SAMPLE_SEARCH_TERM)).thenReturn(contents);

        doNothing().when(dbClient).createContents(contentsDocument);
        when(dbClient.getContents(anyString())).thenReturn(contents);
        var input = new ContentsRequest(contentsDocument);
        var actual = mockedApiHandler.processInput(input, requestInfo, mock(Context.class));
        assertEquals(contentsDocument, actual);
    }

    @Test
    void handlerReturnsBadRequestExceptionWhenMissingIsbn() {
        var exception = assertThrows(BadRequestException.class,
            () -> mockedApiHandler.validateRequest(null, requestInfo, mock(Context.class)));
        assertEquals("No parameters given to CreateContentsApiHandler", exception.getMessage());
    }


}
