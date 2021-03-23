package no.unit.bibs.contents;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.IoUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static nva.commons.handlers.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;
import static nva.commons.utils.JsonUtils.objectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateContentsApiHandlerTest {

    public static final String CREATE_CONTENTS_EVENT = "createContentsEvent.json";

    @Test
    public void processInputTest() throws ApiGatewayException, JsonProcessingException {
        DynamoDBClient client = mock(DynamoDBClient.class);
        Environment environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        UpdateContentsApiHandler handler = new UpdateContentsApiHandler(environment, client);
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        ContentsRequest request = new ContentsRequest(contents);
        ContentsDocument contentsDocument = objectMapper.readValue(contents, ContentsDocument.class);
        when(client.updateContents(contentsDocument)).thenReturn(contents);
        GatewayResponse gatewayResponse = handler.processInput(request, new RequestInfo(), mock(Context.class));
        assertEquals(HttpStatus.SC_CREATED, gatewayResponse.getStatusCode());
    }


}