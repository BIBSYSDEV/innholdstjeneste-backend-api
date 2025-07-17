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
import static no.unit.nva.hamcrest.PropertyValuePair.EMPTY_STRING;
import static nva.commons.apigateway.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateContentsApiHandlerTest {

    public static final String CREATE_CONTENTS_EVENT = "createContentsEvent.json";
    public static final String TEST_ISBN = "9788205377547";
    private Environment environment;
    private CreateContentsApiHandler handler;
    private Context context;
    private DBClient dbClient;
    private StorageClient storageClient;
    private RequestInfo requestInfo;


    /**
     * javadoc for checkstyle.
     */
    @BeforeEach
    public void init() {
        environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        when(environment.readEnv("DYNAMODB_TABLE_NAME_ENV"))
            .thenReturn("TEST_TABLE_NAME");
        when(environment.readEnv("S3_BUCKET_NAME_ENV"))
            .thenReturn("TEST_BUCKET_NAME");
        when(environment.readEnv("COGNITO_AUTHORIZER_URLS"))
            .thenReturn("https://test.cognito.auth.url");
        when(environment.readEnv("COGNITO_AUTHORIZER_NAME"))
            .thenReturn("testCognitoAuthorizerName");

        requestInfo = mock(RequestInfo.class);
        when(requestInfo.getQueryParameters()).thenReturn(Map.of("isbn", TEST_ISBN));

        context = mock(Context.class);
        dbClient = mock(DBClient.class);
        storageClient = mock(StorageClient.class);
        handler = new CreateContentsApiHandler(environment, dbClient, storageClient);
    }

    @Test
    void getSuccessStatusCodeReturnsOK() {
        var statusCode = handler.getSuccessStatusCode(null, null);
        assertEquals(HttpURLConnection.HTTP_CREATED, statusCode);
    }

    @Test
    void handlerReturnsSearchResultsWhenQueryIsSingleTerm() throws ApiGatewayException, JsonProcessingException {

        var contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        var contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        doNothing().when(dbClient).createContents(contentsDocument);
        when(dbClient.getContents(anyString())).thenReturn(contents);
        var request = new ContentsRequest(contentsDocument);
        var actual = handler.processInput(request, requestInfo, context);
        assertEquals(contentsDocument, actual);
    }

    /**
     * Test for handlerReturnsErrorWithEmptyContentsDocument.
     */
    @Test
    void handlerReturnsErrorWithEmptyContentsDocument() throws ApiGatewayException, JsonProcessingException {
        var contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT))
            .replace(TEST_ISBN, EMPTY_STRING);
        var contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        doNothing()
            .when(dbClient)
            .createContents(contentsDocument);
        when(dbClient.getContents(anyString()))
            .thenReturn(contents);
        var request = new ContentsRequest(contentsDocument);
        var handler = new CreateContentsApiHandler(environment, dbClient, storageClient);
        var exception = assertThrows(BadRequestException.class,
            () -> handler.processInput(request, requestInfo, context));
        assertTrue(exception.getMessage().contains(CreateContentsApiHandler.COULD_NOT_INDEX_RECORD_PROVIDED));
    }

    @Test
    void handlerThrowsExceptionWithEmptyRequest() {
        var exception =
            assertThrows(BadRequestException.class,
                () -> handler.validateRequest(null, requestInfo, context));
        assertTrue(exception.getMessage().contains(CreateContentsApiHandler.NO_PARAMETERS_GIVEN_TO_HANDLER));
    }

}
