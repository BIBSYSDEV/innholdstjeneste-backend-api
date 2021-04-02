package no.unit.bibs.contents;

import com.amazonaws.services.lambda.runtime.Context;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.IoUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static no.unit.bibs.contents.GetContentFileApiHandler.*;
import static nva.commons.handlers.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetContentFileApiHandlerTest {

    private Environment environment;
    private GetContentFileApiHandler getContentFileApiHandler;

    public static final String SAMPLE_FIRST_LINK_PART = "1";
    public static final String SAMPLE_SECOND_LINK_PART = "2";
    public static final String SAMPLE_FILENAME = "filename.extension";
    public static final String SAMPLE_TYPE = "type";

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
        GatewayResponse response =  new GatewayResponse(environment, null, HttpStatus.SC_MOVED_PERMANENTLY);
        Integer statusCode = getContentFileApiHandler.getSuccessStatusCode(null, response);
        assertEquals(statusCode, HttpStatus.SC_MOVED_PERMANENTLY);
    }

    @Test
    void handlerReturnsStatusAndLocationHeaderBasedOnPathParameters() throws ApiGatewayException {
        GetContentFileApiHandler handler = new GetContentFileApiHandler(environment);
        GatewayResponse gatewayResponse = handler.processInput(null, getRequestInfo(), mock(Context.class));
        int actualStatusCode = gatewayResponse.getStatusCode();
        String actuaLocation = gatewayResponse.getHeaders().get(HttpHeaders.LOCATION);
        String expectedLocation = String.format(BUCKET_URL_TEMPLATE, MOCK_BUCKET_NAME, MOCK_AWS_REGION, SAMPLE_TYPE,
                SAMPLE_FIRST_LINK_PART, SAMPLE_SECOND_LINK_PART, SAMPLE_FILENAME);
        assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, actualStatusCode);
        assertEquals(expectedLocation, actuaLocation);
    }

    private RequestInfo getRequestInfo() {
        var requestInfo = new RequestInfo();
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put(GetContentFileApiHandler.PATH_PARAMETER_FILENAME, SAMPLE_FILENAME);
        pathParameters.put(GetContentFileApiHandler.PATH_PARAMETER_FIRST_LINK_PART, SAMPLE_FIRST_LINK_PART);
        pathParameters.put(GetContentFileApiHandler.PATH_PARAMETER_SECOND_LINK_PART, SAMPLE_SECOND_LINK_PART);
        pathParameters.put(GetContentFileApiHandler.PATH_PARAMETER_TYPE, SAMPLE_TYPE);
        requestInfo.setPathParameters(pathParameters);
        return requestInfo;
    }

}
