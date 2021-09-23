package no.unit.bibs.contents;

import static no.unit.bibs.contents.GetContentFileApiHandler.AWS_REGION;
import static no.unit.bibs.contents.GetContentFileApiHandler.BUCKET_NAME;
import static no.unit.bibs.contents.GetContentFileApiHandler.BUCKET_URL_TEMPLATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GetContentFileApiHandlerTest {

    private Environment environment;
    private GetContentFileApiHandler getContentFileApiHandler;

    public static final String SAMPLE_FIRST_LINK_PART = "1";
    public static final String SAMPLE_SECOND_LINK_PART = "2";
    public static final String SAMPLE_FILENAME = "filename.extension";
    public static final String SAMPLE_TYPE = "type";
    public static final String SAMPLE_SUBTYPE = "subtype";

    private static final String MOCK_BUCKET_NAME = "test-bucket";
    private static final String MOCK_AWS_REGION = "eu-west-1";


    private void initEnvironment() {
        environment = mock(Environment.class);
        when(environment.readEnv(BUCKET_NAME)).thenReturn(MOCK_BUCKET_NAME);
        when(environment.readEnv(AWS_REGION)).thenReturn(MOCK_AWS_REGION);
    }

    @BeforeEach
    public void init() {
        initEnvironment();
        getContentFileApiHandler = new GetContentFileApiHandler(environment);
    }

    @Test
    void getSuccessStatusCodeReturnsMovedPermanently() {
        Integer statusCode = getContentFileApiHandler.getSuccessStatusCode(null, null);
        assertEquals(statusCode, HttpURLConnection.HTTP_MOVED_PERM);
    }

    @Test
    void handlerReturnsStatusAndLocationHeaderBasedOnPathParameters() {
        GetContentFileApiHandler handler = new GetContentFileApiHandler(environment);
        GatewayResponse gatewayResponse = handler.processInput(null, getRequestInfo(), mock(Context.class));
        int actualStatusCode = gatewayResponse.getStatusCode();
        String actuaLocation = gatewayResponse.getHeaders().get(HttpHeaders.LOCATION);
        String expectedLocation = String.format(BUCKET_URL_TEMPLATE, MOCK_BUCKET_NAME, MOCK_AWS_REGION, SAMPLE_TYPE,
                SAMPLE_SUBTYPE, SAMPLE_FIRST_LINK_PART, SAMPLE_SECOND_LINK_PART, SAMPLE_FILENAME);
        assertEquals(HttpURLConnection.HTTP_MOVED_PERM, actualStatusCode);
        assertEquals(expectedLocation, actuaLocation);
    }

    private RequestInfo getRequestInfo() {
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put(GetContentFileApiHandler.PATH_PARAMETER_FILENAME, SAMPLE_FILENAME);
        pathParameters.put(GetContentFileApiHandler.PATH_PARAMETER_FIRST_LINK_PART, SAMPLE_FIRST_LINK_PART);
        pathParameters.put(GetContentFileApiHandler.PATH_PARAMETER_SECOND_LINK_PART, SAMPLE_SECOND_LINK_PART);
        pathParameters.put(GetContentFileApiHandler.PATH_PARAMETER_SUBTYPE, SAMPLE_SUBTYPE);
        pathParameters.put(GetContentFileApiHandler.PATH_PARAMETER_TYPE, SAMPLE_TYPE);
        var requestInfo = new RequestInfo();
        requestInfo.setPathParameters(pathParameters);
        return requestInfo;
    }

}
