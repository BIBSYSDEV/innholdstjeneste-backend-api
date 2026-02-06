package no.unit.bibs.contents;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.nio.file.Path;

import static no.unit.bibs.contents.GetContentsApiHandler.ISBN;
import static no.unit.bibs.contents.GetContentsApiHandler.MISSING_REQUIRED_QUERY_PARAMETER;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.apigateway.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetContentsApiHandlerTest {

    public static final String QUERY_PARAM_ISBN = "12345678";
    public static final String COGNITO_AUTHORIZER_URLS = "COGNITO_AUTHORIZER_URLS";
    private RequestInfo requestInfo;
    private DBClient dbClient;
    private GetContentsApiHandler mockedApiHandler;


    @BeforeEach
    public void init() {
        requestInfo = mock(RequestInfo.class);
        dbClient = mock(DBClient.class);
        var environment = mock(Environment.class);

        when(environment.readEnv(ALLOWED_ORIGIN_ENV))
            .thenReturn("*");
        when(environment.readEnv(COGNITO_AUTHORIZER_URLS))
            .thenReturn("https://test.cognito.auth.url");

        mockedApiHandler = new GetContentsApiHandler(environment, dbClient);
    }

    @Test
    void getSuccessStatusCodeReturnsOK() {
        var statusCode = mockedApiHandler.getSuccessStatusCode(null, null);
        assertEquals(HttpURLConnection.HTTP_OK, statusCode);
    }

    @Test
    void handlerReturnsContentsDocumentByGivenTerm() throws ApiGatewayException, JsonProcessingException {
        var contents = IoUtils.stringFromResources(Path.of(DBClientTest.GET_CONTENTS_JSON));
        var contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);

        when(requestInfo.getQueryParameter(anyString()))
            .thenReturn(QUERY_PARAM_ISBN);
        when(dbClient.getContents(anyString()))
            .thenReturn(contents);

        var actual = mockedApiHandler.processInput(mock(Void.class), requestInfo, mock(Context.class));
        assertEquals(contentsDocument, actual);
    }

    @Test
    void handlerReturnsBadRequestExceptionWhenMissingIsbn() {
        var exception = assertThrows(BadRequestException.class,
            () -> mockedApiHandler.validateRequest(mock(Void.class), requestInfo, mock(Context.class)));
        assertEquals(MISSING_REQUIRED_QUERY_PARAMETER + ISBN, exception.getMessage());
    }


}
