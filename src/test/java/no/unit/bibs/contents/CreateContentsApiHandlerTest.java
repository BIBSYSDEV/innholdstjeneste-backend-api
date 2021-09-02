package no.unit.bibs.contents;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.bibs.contents.exception.ParameterException;
import no.unit.nva.testutils.HandlerRequestBuilder;
import no.unit.nva.testutils.IoUtils;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static no.unit.nva.hamcrest.PropertyValuePair.EMPTY_STRING;
import static nva.commons.apigateway.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;
import static nva.commons.core.JsonUtils.objectMapper;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateContentsApiHandlerTest {

    public static final String MESSAGE = "message";
    public static final String CREATE_CONTENTS_EVENT = "createContentsEvent.json";
    public static final String TEST_ISBN = "9788205377547";
    private Environment environment;
    private CreateContentsApiHandler handler;
    private Context context;
    private DynamoDBClient dynamoDBClient;
    private S3Client s3Client;


    /**
     * javadoc for checkstyle.
     */
    @BeforeEach
    public void init() {
        environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        dynamoDBClient = mock(DynamoDBClient.class);
        s3Client = mock(S3Client.class);
        handler = new CreateContentsApiHandler(environment, dynamoDBClient, s3Client);
        this.context = mock(Context.class);
    }

    @Test
    void getSuccessStatusCodeReturnsOK() {
        CreateContentsApiHandler handler = new CreateContentsApiHandler(environment, dynamoDBClient, s3Client);
        var response =  new no.unit.bibs.contents.GatewayResponse(environment, MESSAGE, HttpStatus.SC_OK);
        Integer statusCode = handler.getSuccessStatusCode(null, response);
        assertEquals(statusCode, HttpStatus.SC_OK);
    }

    @Test
    void handlerReturnsSearchResultsWhenQueryIsSingleTerm() throws ApiGatewayException, JsonProcessingException {
        var handler = new CreateContentsApiHandler(environment, dynamoDBClient, s3Client);
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        ContentsDocument contentsDocument = objectMapper.readValue(contents, ContentsDocument.class);
        doNothing().when(dynamoDBClient).createContents(contentsDocument);
        when(dynamoDBClient.getContents(anyString())).thenReturn(contents);
        ContentsRequest request = new ContentsRequest(contentsDocument);
        var actual = handler.processInput(request, new RequestInfo(), mock(Context.class));
        assertEquals(contents, actual.getBody());
    }

    @Test
    void handlerReturnsErrorWhithEmptyContentsDocument() throws ApiGatewayException, JsonProcessingException {
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        contents = contents.replace(TEST_ISBN, EMPTY_STRING);
        ContentsDocument contentsDocument = objectMapper.readValue(contents, ContentsDocument.class);
        doNothing().when(dynamoDBClient).createContents(contentsDocument);
        when(dynamoDBClient.getContents(anyString())).thenReturn(contents);
        ContentsRequest request = new ContentsRequest(contentsDocument);
        var handler = new CreateContentsApiHandler(environment, dynamoDBClient, s3Client);
        var actual = handler.processInput(request, new RequestInfo(), mock(Context.class));
        assertTrue(actual.getBody().contains(CreateContentsApiHandler.COULD_NOT_INDEX_RECORD_PROVIDED));
    }

    @Test
    void handlerThrowsExceptionWithEmptyRequest()  {
        Exception exception = assertThrows(ParameterException.class, () -> {
            handler.processInput(null, new RequestInfo(), mock(Context.class));
        });

        assertTrue(exception.getMessage().contains(CreateContentsApiHandler.NO_PARAMETERS_GIVEN_TO_HANDLER));
    }


    @Test
    public void handleRequestReturnsCreatedIfContentsIsPosted() throws IOException {
        ContentsRequest request = readMockCreateContentsRequestFromJsonFile();
        GatewayResponse<Void> response = sendRequest(request);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.SC_CREATED)));
    }

    private <T> GatewayResponse<T> sendRequest(ContentsRequest request) throws IOException {
        InputStream input = createRequest(request);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler.handleRequest(input, output, context);

        return GatewayResponse.fromOutputStream(output);
    }

    private InputStream createRequest(ContentsRequest request) throws JsonProcessingException {
        return new HandlerRequestBuilder<ContentsRequest>(objectMapper)
                .withBody(request)
                .build();
    }

    private ContentsRequest readMockCreateContentsRequestFromJsonFile() throws JsonProcessingException {
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        ContentsDocument contentsDocument = objectMapper.readValue(contents, ContentsDocument.class);
        return new ContentsRequest(contentsDocument);
    }
}
