package no.unit.bibs.contents;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.unit.bibs.contents.exception.ParameterException;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.exceptions.commonexceptions.NotFoundException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.handlers.RestRequestHandler;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.JsonUtils;
import nva.commons.utils.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static java.util.Objects.isNull;

public class UpdateContentsApiHandler extends ApiGatewayHandler<ContentsRequest, GatewayResponse> {

    public static final String NO_PARAMETERS_GIVEN_TO_HANDLER = "No parameters given to ContentsApiHandler";
    public static final String COULD_NOT_INDEX_RECORD_PROVIDED = "Could not index record provided. ";


    private static final ObjectMapper mapper = JsonUtils.objectMapper;
    private final DynamoDBClient dynamoDBClient;

    @JacocoGenerated
    public UpdateContentsApiHandler() {
        this(new Environment());
    }

    public UpdateContentsApiHandler(Environment environment) {
        this(environment, new DynamoDBClient(environment));
    }

    public UpdateContentsApiHandler(Environment environment, DynamoDBClient dynamoDBClient) {
        super(ContentsRequest.class, environment, LoggerFactory.getLogger(UpdateContentsApiHandler.class));
        this.dynamoDBClient = dynamoDBClient;
    }


    /**
     * Implements the main logic of the handler. Any exception thrown by this method will be handled by {@link
     * RestRequestHandler#handleExpectedException} method.
     *
     * @param request       The input object to the method. Usually a deserialized json.
     * @param requestInfo Request headers and path.
     * @param context     the ApiGateway context.ucket
     * @return the Response body that is going to be serialized in json
     * @throws ApiGatewayException all exceptions are caught by writeFailure and mapped to error codes through the
     *                             method {@link RestRequestHandler#getFailureStatusCode}
     */
    @Override
    protected GatewayResponse processInput(ContentsRequest request, RequestInfo requestInfo,
                                           Context context) throws ApiGatewayException {
        if (isNull(request)) {
            throw new ParameterException(NO_PARAMETERS_GIVEN_TO_HANDLER);
        }
        String json = request.getContents();
        logger.error("json input looks like that :" + json);
        Optional<ContentsDocument> contentsDocument = fromJsonString(json);
        GatewayResponse gatewayResponse = new GatewayResponse(environment);
        if (contentsDocument.isPresent()) {
            logger.error("This is my IndexDocument to index: " + contentsDocument.toString());
            try {
                String contents = dynamoDBClient.getContents(contentsDocument.get().getIsbn());
                if (StringUtils.isNotEmpty(contents)) {
                    gatewayResponse.setBody(dynamoDBClient.createContents(contentsDocument.get()));
                    gatewayResponse.setStatusCode(HttpStatus.SC_CREATED);
                } else {
                    gatewayResponse.setBody(dynamoDBClient.updateContents(contentsDocument.get()));
                    gatewayResponse.setStatusCode(HttpStatus.SC_OK);
                }
            } catch (NotFoundException e) {
                gatewayResponse.setBody(dynamoDBClient.updateContents(contentsDocument.get()));
                gatewayResponse.setStatusCode(HttpStatus.SC_OK);
            }
        } else {
            logger.error(COULD_NOT_INDEX_RECORD_PROVIDED + json);
            gatewayResponse.setErrorBody(COULD_NOT_INDEX_RECORD_PROVIDED + json);
            gatewayResponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
        }
        return gatewayResponse;
    }

    private Optional<ContentsDocument> fromJsonString(String line) {
        try {
            ContentsDocument contentsDocument = mapper.readValue(line, ContentsDocument.class);
            return Optional.of(contentsDocument);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Define the success status code.
     *
     * @param input  The request input.
     * @param output The response output
     * @return the success status code.
     */
    @Override
    protected Integer getSuccessStatusCode(ContentsRequest input, GatewayResponse output) {
        return output.getStatusCode();
    }
}
