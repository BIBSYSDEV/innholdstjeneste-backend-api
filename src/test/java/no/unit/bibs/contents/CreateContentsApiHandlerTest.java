package no.unit.bibs.contents;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.bibs.contents.exception.ParameterException;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.GatewayResponse;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.IoUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static nva.commons.handlers.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;
import static nva.commons.utils.JsonUtils.objectMapper;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateContentsApiHandlerTest {

    public static final String MESSAGE = "message";
    public static final String CREATE_CONTENTS_EVENT = "createContentsEvent.json";
    private Environment environment;
    private CreateContentsApiHandler handler;
    private Context context;
    private DynamoDBClient dynamoDBClient;


    /**
     * javadoc for checkstyle.
     */
    @BeforeEach
    public void init() {
        environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        dynamoDBClient = mock(DynamoDBClient.class);
        handler = new CreateContentsApiHandler(environment, dynamoDBClient);
        this.context = mock(Context.class);
    }

    @Test
    void getSuccessStatusCodeReturnsOK() {
        CreateContentsApiHandler handler = new CreateContentsApiHandler(environment);
        var response =  new no.unit.bibs.contents.GatewayResponse(environment, MESSAGE, HttpStatus.SC_OK);
        Integer statusCode = handler.getSuccessStatusCode(null, response);
        assertEquals(statusCode, HttpStatus.SC_OK);
    }

    @Test
    void handlerReturnsSearchResultsWhenQueryIsSingleTerm() throws ApiGatewayException, JsonProcessingException {
        var handler = new CreateContentsApiHandler(environment, dynamoDBClient);
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
        var handler = new CreateContentsApiHandler(environment, dynamoDBClient);
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        contents = contents.replace("9788205377547", "");
        ContentsDocument contentsDocument = objectMapper.readValue(contents, ContentsDocument.class);
        doNothing().when(dynamoDBClient).createContents(contentsDocument);
        when(dynamoDBClient.getContents(anyString())).thenReturn(contents);
        ContentsRequest request = new ContentsRequest(contentsDocument);
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
