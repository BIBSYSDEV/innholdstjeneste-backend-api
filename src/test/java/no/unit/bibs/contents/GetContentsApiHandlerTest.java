package no.unit.bibs.contents;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.bibs.contents.GetContentsApiHandler.ISBN;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.apigateway.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.GatewayResponseSerializingException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GetContentsApiHandlerTest {

    private static final String GET_CONTENTS_JSON = "get_contents.json";
    private static final String QUERY_PARAM_ISBN = "12345678";
    private static final String COGNITO_AUTHORIZER_URLS = "COGNITO_AUTHORIZER_URLS";
    private static final String MISSING_REQUIRED_QUERY_PARAMETER_ISBN =
        "Missing from query parameters: isbn";
    private static final ObjectMapper OBJECT_MAPPER = JsonUtils.dtoObjectMapper;
    private DBClient dbClient;
    private Context context;
    private ByteArrayOutputStream output;
    private GetContentsApiHandler handler;


    @BeforeEach
    public void init() {
        dbClient = mock(DBClient.class);
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        var environment = mock(Environment.class);

        when(environment.readEnv(ALLOWED_ORIGIN_ENV))
            .thenReturn("*");
        when(environment.readEnv(COGNITO_AUTHORIZER_URLS))
            .thenReturn("https://test.cognito.auth.url");

        handler = new GetContentsApiHandler(environment, dbClient);
    }

    @Test
    void shouldReturnDocumentWhenHavingValidIsbn() throws IOException, NotFoundException {
        var queryParams = Map.of(ISBN, QUERY_PARAM_ISBN);

        var contents = getContentsString();
        var contentsDocument = getContentsDocument(contents);
        doReturn(contents).when(dbClient).getContents(QUERY_PARAM_ISBN);

        var response = sendQuery(queryParams);
        var responseBody = response.getBodyObject(ContentsDocument.class);

        assertThat(response.getStatusCode(), equalTo(HTTP_OK));
        assertThat(responseBody, equalTo(contentsDocument));
        assertThat(responseBody.getIsbn(), equalTo(contentsDocument.getIsbn()));
        assertThat(responseBody.getDescriptionLong(), equalTo(contentsDocument.getDescriptionLong()));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenMissingIsbn() throws IOException {
        var queryParams = Map.of("notAnIsbn", "someValue");

        var response = sendQuery(queryParams);

        assertThat(response.getStatusCode(), equalTo(HTTP_BAD_REQUEST));
        assertThat(response.getBody(), containsString(MISSING_REQUIRED_QUERY_PARAMETER_ISBN));
    }

    @Test
    void shouldThrowSerializingExceptionWhenContentFoundCouldNotBeDeserialized() throws IOException,
                                                                                        NotFoundException {
        var queryParams = Map.of(ISBN, QUERY_PARAM_ISBN);
        doReturn("invalid-json").when(dbClient).getContents(QUERY_PARAM_ISBN);

        var response = sendQuery(queryParams);

        assertThat(response.getStatusCode(), equalTo(HTTP_INTERNAL_ERROR));
        assertThat(response.getBody(), containsString(GatewayResponseSerializingException.ERROR_MESSAGE));
    }

    private GatewayResponse<ContentsDocument> sendQuery(Map<String, String> queryParams) throws IOException {
        var input = requestWithQueryParameters(queryParams);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, ContentsDocument.class);
    }

    private InputStream requestWithQueryParameters(Map<String, String> map) throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
                   .withBody(null)
                   .withQueryParameters(map)
                   .build();
    }

    private ContentsDocument getContentsDocument(String contents) throws JsonProcessingException {
        return dtoObjectMapper.readValue(contents, ContentsDocument.class);
    }

    private String getContentsString() {
        return IoUtils.stringFromResources(Path.of(GET_CONTENTS_JSON));
    }

}
