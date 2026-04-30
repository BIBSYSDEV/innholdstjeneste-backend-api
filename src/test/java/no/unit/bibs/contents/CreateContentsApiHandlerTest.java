package no.unit.bibs.contents;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import no.unit.bibs.contents.exception.CommunicationException;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.GatewayResponseSerializingException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static no.unit.bibs.contents.ContentsRequest.DOCUMENT_JSON_NOT_VALID;
import static no.unit.bibs.contents.ContentsRequest.MALFORMED_JSON_PAYLOAD;
import static no.unit.bibs.contents.CreateContentsApiHandler.COULD_NOT_INDEX_RECORD_PROVIDED;
import static no.unit.bibs.contents.CreateContentsApiHandler.NO_PARAMETERS_GIVEN_TO_HANDLER;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.apigateway.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateContentsApiHandlerTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonUtils.dtoObjectMapper;
    private static final String CREATE_CONTENTS_EVENT = "createContentsEvent.json";
    private static final String TEST_ISBN = "9788205377547";

    private Context context;
    private ByteArrayOutputStream output;
    private DBClient dbClient;
    private CreateContentsApiHandler handler;

    @BeforeEach
    void init() {
        var environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        when(environment.readEnv("DYNAMODB_TABLE_NAME_ENV"))
            .thenReturn("TEST_TABLE_NAME");
        when(environment.readEnv("S3_BUCKET_NAME_ENV"))
            .thenReturn("TEST_BUCKET_NAME");
        when(environment.readEnv("COGNITO_AUTHORIZER_URLS"))
            .thenReturn("https://test.cognito.auth.url");
        when(environment.readEnv("COGNITO_AUTHORIZER_NAME"))
            .thenReturn("testCognitoAuthorizerName");

        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        dbClient = mock(DBClient.class);
        var storageClient = mock(StorageClient.class);

        handler = new CreateContentsApiHandler(environment, dbClient, storageClient);
    }

    @Test
    void shouldCreateContentsRequestResourceFromInputAndReturnHttpCodeCreated() throws Exception {
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
    void shouldThrowBadRequestExceptionWithEmptyRequest() throws IOException {
        var response = sendQuery(null);

        assertThat(response.getStatusCode(), equalTo(HTTP_BAD_REQUEST));
        assertThat(response.getBody(), containsString(NO_PARAMETERS_GIVEN_TO_HANDLER));
    }

    @Test
    void shouldThrowBadRequestExceptionOnMalformedPayload() throws IOException {
        var contentsRequest = mock(ContentsRequest.class);
        doReturn(null).when(contentsRequest).getContents();

        var response = sendQuery(contentsRequest);

        assertThat(response.getStatusCode(), equalTo(HTTP_BAD_REQUEST));
        assertThat(response.getBody(), containsString(MALFORMED_JSON_PAYLOAD));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenInvalidContentsDocument() throws Exception {
        var contents = getContentsString().replace(TEST_ISBN, EMPTY_STRING);
        var contentsDocument = getContentsDocument(contents);
        var contentsRequest = new ContentsRequest(contentsDocument);

        var response = sendQuery(contentsRequest);

        assertThat(response.getStatusCode(), equalTo(HTTP_BAD_REQUEST));
        assertThat(response.getBody(), containsString(DOCUMENT_JSON_NOT_VALID));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenContentsDocumentFailedToPersist() throws Exception {
        var contents = getContentsString();
        var contentsDocument = getContentsDocument(contents);
        var contentsRequest = new ContentsRequest(contentsDocument);

        doThrow(CommunicationException.class).when(dbClient).createContents(contentsDocument);

        var response = sendQuery(contentsRequest);

        assertThat(response.getStatusCode(), equalTo(HTTP_BAD_REQUEST));
        assertThat(response.getBody(), containsString(COULD_NOT_INDEX_RECORD_PROVIDED));
    }

    @Test
    void shouldThrowInternalErrorExceptionWhenContentsDocumentFailedToDeserialize() throws Exception {
        var contents = getContentsString();
        var contentsDocument = getContentsDocument(contents);
        var contentsRequest = new ContentsRequest(contentsDocument);

        doNothing().when(dbClient).createContents(contentsDocument);
        when(dbClient.getContents(anyString())).thenReturn("invalid-json");

        var response = sendQuery(contentsRequest);

        assertThat(response.getStatusCode(), equalTo(HTTP_INTERNAL_ERROR));
        assertThat(response.getBody(), containsString(GatewayResponseSerializingException.ERROR_MESSAGE));
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
