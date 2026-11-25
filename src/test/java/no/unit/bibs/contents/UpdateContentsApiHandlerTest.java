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
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static no.unit.bibs.contents.ContentsRequest.DOCUMENT_JSON_NOT_VALID;
import static no.unit.bibs.contents.ContentsRequest.MALFORMED_JSON_PAYLOAD;
import static no.unit.bibs.contents.UpdateContentsApiHandler.FAILED_AFTER_PERSISTING;
import static no.unit.bibs.contents.UpdateContentsApiHandler.NO_PARAMETERS_GIVEN_TO_HANDLER;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.apigateway.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateContentsApiHandlerTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonUtils.dtoObjectMapper;
    private static final String CREATE_CONTENTS_EVENT = "createContentsEvent.json";
    private static final String COGNITO_AUTHORIZER_URLS = "COGNITO_AUTHORIZER_URLS";
    private static final String TEST_ISBN = "9788205377547";

    private DBClient dbClient;
    private Context context;
    private ByteArrayOutputStream output;
    private UpdateContentsApiHandler handler;

    @BeforeEach
    void init() {
        var environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV))
            .thenReturn("*");
        when(environment.readEnv(COGNITO_AUTHORIZER_URLS))
            .thenReturn("https://test.cognito.auth.url");

        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        dbClient = mock(DBClient.class);

        handler = new UpdateContentsApiHandler(environment, dbClient, mock(StorageClient.class));
    }

    @Test
    void shouldCreateContentsRequestResourceFromInputAndReturnHttpCodeCreatedWhenGetContentFromResourceReturnsEmpty()
        throws Exception {

        var contents = getContentsString();
        var contentsDocument = getContentsDocument(contents);
        var contentsRequest = new ContentsRequest(contentsDocument);

        when(dbClient.getContents(anyString())).thenReturn(null).thenReturn(contents);

        var response = sendQuery(contentsRequest);

        verify(dbClient, times(1)).createContents(contentsDocument);
        verify(dbClient, times(0)).updateContents(contentsDocument);
        assertThat(response.getStatusCode(), equalTo(HTTP_CREATED));

        var responseBody = response.getBodyObject(ContentsDocument.class);

        assertThat(responseBody, equalTo(contentsDocument));
        assertThat(responseBody.getIsbn(), equalTo(contentsDocument.getIsbn()));
        assertThat(responseBody.getDescriptionLong(), equalTo(contentsDocument.getDescriptionLong()));
    }

    @Test
    void shouldCreateContentsRequestResourceFromInputWhenGetContentsThrowNotFoundExceptionIndicatingNotAlreadyExists()
        throws Exception {

        var contents = getContentsString();
        var contentsDocument = getContentsDocument(contents);
        var contentsRequest = new ContentsRequest(contentsDocument);

        when(dbClient.getContents(anyString())).thenThrow(NotFoundException.class).thenReturn(contents);

        var response = sendQuery(contentsRequest);

        verify(dbClient, times(1)).createContents(contentsDocument);
        verify(dbClient, times(0)).updateContents(contentsDocument);
        assertThat(response.getStatusCode(), equalTo(HTTP_CREATED));

        var responseBody = response.getBodyObject(ContentsDocument.class);

        assertThat(responseBody, equalTo(contentsDocument));
        assertThat(responseBody.getIsbn(), equalTo(contentsDocument.getIsbn()));
        assertThat(responseBody.getDescriptionLong(), equalTo(contentsDocument.getDescriptionLong()));
    }

    @Test
    void shouldThrowConflictExceptionWhenCreateContentsRequestFromInputCouldNotBePersisted()
        throws Exception {

        var contents = getContentsString();
        var contentsDocument = getContentsDocument(contents);
        var contentsRequest = new ContentsRequest(contentsDocument);

        doThrow(CommunicationException.class).when(dbClient).updateContents(contentsDocument);
        when(dbClient.getContents(anyString())).thenReturn(contents);

        var response = sendQuery(contentsRequest);

        assertThat(response.getStatusCode(), equalTo(HTTP_CONFLICT));
        assertThat(response.getBody(), containsString(FAILED_AFTER_PERSISTING));
    }

    @Test
    void shouldUpdateContentsRequestResourceFromInputAndReturnHttpCodeCreatedWhenExistingFromBefore()
        throws Exception {

        var contents = getContentsString();
        var contentsDocument = getContentsDocument(contents);
        var contentsRequest = new ContentsRequest(contentsDocument);

        when(dbClient.getContents(anyString())).thenReturn(contents);

        var response = sendQuery(contentsRequest);

        verify(dbClient, times(0)).createContents(contentsDocument);
        verify(dbClient, times(1)).updateContents(contentsDocument);
        assertThat(response.getStatusCode(), equalTo(HTTP_CREATED));

        var responseBody = response.getBodyObject(ContentsDocument.class);

        assertThat(responseBody, equalTo(contentsDocument));
        assertThat(responseBody.getIsbn(), equalTo(contentsDocument.getIsbn()));
        assertThat(responseBody.getDescriptionLong(), equalTo(contentsDocument.getDescriptionLong()));
    }

    @Test
    void shouldThrowConflictErrorCausedByInternalErrorExceptionWhenContentsDocumentFailedToDeserializeOnUpdate()
        throws Exception {

        var contents = getContentsString();
        var contentsDocument = getContentsDocument(contents);
        var contentsRequest = new ContentsRequest(contentsDocument);

        when(dbClient.getContents(anyString())).thenReturn(contents).thenReturn("invalid-json");

        var response = sendQuery(contentsRequest);

        verify(dbClient, times(0)).createContents(contentsDocument);
        verify(dbClient, times(1)).updateContents(contentsDocument);
        assertThat(response.getStatusCode(), equalTo(HTTP_CONFLICT));
        assertThat(response.getBody(), containsString(GatewayResponseSerializingException.ERROR_MESSAGE));
    }

    @Test
    void shouldThrowConflictErrorCausedByInternalErrorExceptionWhenContentsDocumentFailedToDeserializeOnCreation()
        throws Exception {

        var contents = getContentsString();
        var contentsDocument = getContentsDocument(contents);
        var contentsRequest = new ContentsRequest(contentsDocument);

        when(dbClient.getContents(anyString())).thenReturn(null).thenReturn("invalid-json");

        var response = sendQuery(contentsRequest);

        verify(dbClient, times(0)).updateContents(contentsDocument);
        verify(dbClient, times(1)).createContents(contentsDocument);
        assertThat(response.getStatusCode(), equalTo(HTTP_CONFLICT));
        assertThat(response.getBody(), containsString(GatewayResponseSerializingException.ERROR_MESSAGE));
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