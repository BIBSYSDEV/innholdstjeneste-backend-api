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
import nva.commons.apigateway.exceptions.ConflictException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static no.unit.bibs.contents.ContentsDocument.ISBN;
import static no.unit.bibs.contents.ContentsRequest.DOCUMENT_JSON_NOT_VALID;
import static no.unit.bibs.contents.ContentsRequest.MALFORMED_JSON_PAYLOAD;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.apigateway.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateContentsApiHandlerTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonUtils.dtoObjectMapper;
    private static final String CREATE_CONTENTS_EVENT = "createContentsEvent.json";
    private static final String COGNITO_AUTHORIZER_URLS = "COGNITO_AUTHORIZER_URLS";
    private static final String TEST_ISBN = "9788205377547";

    private RequestInfo requestInfo;
    private DBClient dbClient;
    private String contents;
    private Context context;
    private ByteArrayOutputStream output;
    private UpdateContentsApiHandler handler;
    private ContentsDocument contentsDocument;
    private ContentsRequest request;


    /**
     * javadoc for checkstyle.
     */
    @BeforeEach
    public void init() throws JsonProcessingException {
        var environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV))
            .thenReturn("*");
        when(environment.readEnv(COGNITO_AUTHORIZER_URLS))
            .thenReturn("https://test.cognito.auth.url");

        contents = getContentsString();
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        dbClient = mock(DBClient.class);
        requestInfo = mock(RequestInfo.class);
        when(requestInfo.getQueryParameters())
            .thenReturn(Map.of(ISBN, TEST_ISBN));
        contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        request = new ContentsRequest(contentsDocument);

        handler = new UpdateContentsApiHandler(environment, dbClient, mock(StorageClient.class));
    }

    @Test
    void shouldCreateContentsRequestResourceFromInputAndReturnHttpCodeCreatedWhenNotFoundFromBefore()
        throws Exception {

        var contents = getContentsString();
        var contentsDocument = getContentsDocument(contents);
        var contentsRequest = new ContentsRequest(contentsDocument);

        doNothing().when(dbClient).createContents(contentsDocument);
        when(dbClient.getContents(anyString())).thenReturn(null).thenReturn(contents);

        var response = sendQuery(contentsRequest);
        var responseBody = response.getBodyObject(ContentsDocument.class);

        assertThat(response.getStatusCode(), equalTo(HTTP_CREATED));
        assertThat(responseBody, equalTo(contentsDocument));
        assertThat(responseBody.getIsbn(), equalTo(contentsDocument.getIsbn()));
        assertThat(responseBody.getDescriptionLong(), equalTo(contentsDocument.getDescriptionLong()));
    }

    @Test
    void shouldUpdateContentsRequestResourceFromInputAndReturnHttpCodeCreatedWhenExistingFromBefore()
        throws Exception {

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

    @Test
    public void processInputTest() throws ApiGatewayException {

        when(dbClient.getContents(anyString()))
            .thenReturn(contents);

        var actual = handler.processInput(request, requestInfo, context);
        assertEquals(contentsDocument, actual);
    }

    @Test
    public void testEmptyIsbnInContentsDocument() throws JsonProcessingException {
        contents = contents.replace(TEST_ISBN, EMPTY_STRING);
        var contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        var request = new ContentsRequest(contentsDocument);
        var exception = assertThrows(BadRequestException.class,
                                     () -> handler.validateRequest(request, requestInfo, context));

        assertTrue(exception.getMessage().contains(DOCUMENT_JSON_NOT_VALID));
    }

    @Test
    public void testGetContentsNotFoundWithFinalCrashing() throws ApiGatewayException {

        when(dbClient.getContents(anyString())).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () ->
            handler.processInput(request, requestInfo, context)
        );
    }

    @Test
    public void testGetContentsNotFound() throws ApiGatewayException {

        when(dbClient.getContents(anyString()))
            .thenThrow(NotFoundException.class)
            .thenReturn(contents);

        var actual = handler.processInput(request, requestInfo, context);
        assertEquals(contentsDocument, actual);
    }

    @Test
    public void testGetContentsNotFoundThenCrashing() throws ApiGatewayException {

        when(dbClient.getContents(anyString()))
            .thenThrow(IllegalArgumentException.class);

        assertThrows(ConflictException.class, () -> handler.processInput(request, requestInfo, context)
        );
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