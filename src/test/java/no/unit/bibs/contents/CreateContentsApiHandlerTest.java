package no.unit.bibs.contents;

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

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.HttpURLConnection;
import java.nio.file.Path;

import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreateContentsApiHandlerTest {

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
        Integer statusCode = handler.getSuccessStatusCode(null, null);
        assertEquals(statusCode, HttpURLConnection.HTTP_CREATED);
    }

    @Test
    void handlerReturnsSearchResultsWhenQueryIsSingleTerm() throws ApiGatewayException, JsonProcessingException {
        var handler = new CreateContentsApiHandler(environment, dynamoDBClient, s3Client);
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        ContentsDocument contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        doNothing().when(dynamoDBClient).createContents(contentsDocument);
        when(dynamoDBClient.getContents(anyString())).thenReturn(contents);
        ContentsRequest request = new ContentsRequest(contentsDocument);
        var actual = handler.processInput(request, new RequestInfo(), mock(Context.class));
        assertEquals(contentsDocument, actual);
    }

    @Test
    void handlerReturnsErrorWhithEmptyContentsDocument() throws ApiGatewayException, JsonProcessingException {
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        contents = contents.replace(TEST_ISBN, EMPTY_STRING);
        ContentsDocument contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        doNothing().when(dynamoDBClient).createContents(contentsDocument);
        when(dynamoDBClient.getContents(anyString())).thenReturn(contents);
        ContentsRequest request = new ContentsRequest(contentsDocument);
        var handler = new CreateContentsApiHandler(environment, dynamoDBClient, s3Client);
        Exception exception = assertThrows(BadRequestException.class, () -> {
            handler.processInput(request, new RequestInfo(), mock(Context.class));
        });
        assertTrue(exception.getMessage().contains(CreateContentsApiHandler.COULD_NOT_INDEX_RECORD_PROVIDED));
    }

    @Test
    void handlerThrowsExceptionWithEmptyRequest()  {
        var handler = new CreateContentsApiHandler(environment, dynamoDBClient, s3Client);
        Exception exception = assertThrows(BadRequestException.class, () -> {
            handler.processInput(null, new RequestInfo(), mock(Context.class));
        });
        System.out.println();
        assertTrue(exception.getMessage().contains(CreateContentsApiHandler.NO_PARAMETERS_GIVEN_TO_HANDLER));
    }

}
