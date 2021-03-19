package no.unit.bibs.contents;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        var response =  new no.unit.bibs.contents.GatewayResponse(environment, MESSAGE, HttpStatus.SC_OK);
        Integer statusCode = handler.getSuccessStatusCode(null, response);
        assertEquals(statusCode, HttpStatus.SC_OK);
    }

    @Test
    void handlerReturnsSearchResultsWhenQueryIsSingleTerm() throws ApiGatewayException, JsonProcessingException {
        var handler = new CreateContentsApiHandler(environment, dynamoDBClient);
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        ContentsDocument contentsDocument = objectMapper.readValue(contents, ContentsDocument.class);
        when(dynamoDBClient.createContents(contentsDocument)).thenReturn(contents);
        ContentsRequest request = new ContentsRequest(contents);
        var actual = handler.processInput(request, new RequestInfo(), mock(Context.class));
        assertEquals(contents, actual.getBody());
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

    private ContentsRequest readMockCreateContentsRequestFromJsonFile() {
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        return new ContentsRequest(contents);
    }
}
