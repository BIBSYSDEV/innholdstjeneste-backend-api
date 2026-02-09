package no.unit.bibs.contents;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static no.unit.bibs.contents.ContentsRequest.DOCUMENT_JSON_NOT_VALID;
import static no.unit.bibs.contents.ContentsRequest.MALFORMED_JSON_PAYLOAD;
import static no.unit.bibs.contents.GetContentsApiHandler.ISBN;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.apigateway.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateContentsApiHandlerTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonUtils.dtoObjectMapper;
    private static final String CREATE_CONTENTS_EVENT = "createContentsEvent.json";
    private static final String TEST_ISBN = "9788205377547";

    private Environment environment;
    private Context context;
    private ByteArrayOutputStream output;
    private DBClient dbClient;
    private StorageClient storageClient;
    private CreateContentsApiHandler handler;

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
        output = new ByteArrayOutputStream();
        dbClient = mock(DBClient.class);
        storageClient = mock(StorageClient.class);
        handler = new CreateContentsApiHandler(environment, dbClient, storageClient);
    }

    @Test
    void shouldCreateContentsRequestResourceFromInputAndReturnHttpCodeCreated() throws ApiGatewayException,
                                                                                       IOException {
        var contents = getContentsString();
        var contentsDocument = getContentsDocument(contents);
        var contentsRequest = new ContentsRequest(contentsDocument);

        doNothing().when(dbClient).createContents(contentsDocument);
        when(dbClient.getContents(anyString())).thenReturn(contents);

        var response = sendQuery(contentsRequest);
        var responseBody = response.getBodyObject(ContentsDocument.class);

        assertThat(response.getStatusCode(), equalTo(HTTP_CREATED));
        assertThat(responseBody, equalTo(contentsDocument));
        assertThat(responseBody.getIsbn(), equalTo(contentsDocument.getIsbn()));
        assertThat(responseBody.getDescriptionLong(), equalTo(contentsDocument.getDescriptionLong()));
    }

    /**
     * Test for handlerReturnsErrorWithEmptyContentsDocument.
     */
    @Test
    void handlerReturnsErrorWithEmptyContentsDocument() throws ApiGatewayException, JsonProcessingException {
        var contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT))
            .replace(TEST_ISBN, EMPTY_STRING);
        var contentsDocument = getContentsDocument(contents);
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

    @Test
    public void shouldThrowExceptionOnMalformedPayload() {
        var contentsRequest = mock(ContentsRequest.class);
        doReturn(null).when(contentsRequest).getContents();

        var exception = assertThrows(BadRequestException.class,
                                     () -> handler.validateRequest(contentsRequest, requestInfo, context));

        assertTrue(exception.getMessage().contains(MALFORMED_JSON_PAYLOAD));
    }

    @Test
    public void shouldThrowExceptionOnInvalidJson() throws JsonProcessingException {
        var contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT)).replace(TEST_ISBN, EMPTY_STRING);
        var contentsDocument = getContentsDocument(contents);
        var request = new ContentsRequest(contentsDocument);
        var exception = assertThrows(BadRequestException.class,
                                     () -> handler.validateRequest(request, requestInfo, context));

        assertTrue(exception.getMessage().contains(DOCUMENT_JSON_NOT_VALID));
    }

    private GatewayResponse<ContentsDocument> sendQuery(ContentsRequest body)
        throws IOException {

        var input = requestWithQueryParameters(body);
        handler.handleRequest(input, output, context);

        return GatewayResponse.fromOutputStream(output, ContentsDocument.class);
    }

    private InputStream requestWithQueryParameters(ContentsRequest body)
        throws JsonProcessingException {

        return new HandlerRequestBuilder<ContentsRequest>(OBJECT_MAPPER)
                   .withBody(body)
                   .build();
    }

    private ContentsDocument getContentsDocument(String contents) throws JsonProcessingException {
        return dtoObjectMapper.readValue(contents, ContentsDocument.class);
    }

    private String getContentsString() {
        return IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
    }

}
