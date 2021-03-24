package no.unit.bibs.contents;

import static java.util.Objects.isNull;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.bibs.contents.exception.ParameterException;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.exceptions.commonexceptions.NotFoundException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.handlers.RestRequestHandler;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;

public class UpdateContentsApiHandler extends ApiGatewayHandler<ContentsRequest, GatewayResponse> {

    public static final String NO_PARAMETERS_GIVEN_TO_HANDLER = "No parameters given to UpdateContentsApiHandler";
    public static final String COULD_NOT_INDEX_RECORD_PROVIDED = "Could not update provided contents. ";

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
     * @param request     The input object to the method. Usually a deserialized json.
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
        ContentsDocument contentsDocument = request.getContents();
        logger.error("json input looks like that :" + contentsDocument.toString());
        GatewayResponse gatewayResponse = new GatewayResponse(environment);
        try {
            if (StringUtils.isNotEmpty(contentsDocument.getIsbn())) {
                logger.debug("This is my ContentsDocument to persist: " + contentsDocument.toString());
                try {
                    String contents = dynamoDBClient.getContents(contentsDocument.getIsbn());
                    if (StringUtils.isEmpty(contents)) {
                        dynamoDBClient.createContents(contentsDocument);
                        String createContents = dynamoDBClient.getContents(contentsDocument.getIsbn());
                        logger.info("contents created");
                        gatewayResponse.setBody(createContents);
                        gatewayResponse.setStatusCode(HttpStatus.SC_CREATED);
                    } else {
                        String updateContents = dynamoDBClient.updateContents(contentsDocument);
                        logger.info("contents updated");
                        gatewayResponse.setBody(updateContents);
                        gatewayResponse.setStatusCode(HttpStatus.SC_OK);
                    }
                } catch (NotFoundException e) {
                    dynamoDBClient.createContents(contentsDocument);
                    String createdContents = dynamoDBClient.getContents(contentsDocument.getIsbn());
                    logger.info("contents updated");
                    gatewayResponse.setBody(createdContents);
                    gatewayResponse.setStatusCode(HttpStatus.SC_CREATED);
                } catch (Exception e) {
                    String msg = "failed after persisting: " + e.getMessage();
                    logger.error(msg, e);
                    gatewayResponse.setBody(msg);
                    gatewayResponse.setStatusCode(HttpStatus.SC_CONFLICT);
                }
            } else {
                logger.error(COULD_NOT_INDEX_RECORD_PROVIDED + contentsDocument.toString());
                gatewayResponse.setErrorBody(COULD_NOT_INDEX_RECORD_PROVIDED + contentsDocument.toString());
                gatewayResponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            }
        } catch (Exception e) {
            String msg = "error in update function: " + e.getMessage();
            logger.error(msg, e);
            gatewayResponse.setBody(msg);
            gatewayResponse.setStatusCode(HttpStatus.SC_METHOD_FAILURE);
        }
        return gatewayResponse;
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
