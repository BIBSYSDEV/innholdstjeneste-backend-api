package no.unit.bibs.contents;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.bibs.contents.exception.ParameterException;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.handlers.RestRequestHandler;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static java.util.Objects.isNull;

public class CreateContentsApiHandler extends ApiGatewayHandler<ContentsRequest, GatewayResponse> {

    public static final String NO_PARAMETERS_GIVEN_TO_HANDLER = "No parameters given to CreateContentsApiHandler";
    public static final String COULD_NOT_INDEX_RECORD_PROVIDED = "Could not persist provided contents. ";
    public static final String ERROR_STORING_FILE = "error storing file: ";

    private final DynamoDBClient dynamoDBClient;
    private final S3Client s3Client;

    @JacocoGenerated
    public CreateContentsApiHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public CreateContentsApiHandler(Environment environment) {
        this(environment, new DynamoDBClient(environment), new S3Client(environment));
    }

    public CreateContentsApiHandler(Environment environment, DynamoDBClient dynamoDBClient, S3Client s3Client) {
        super(ContentsRequest.class, environment, LoggerFactory.getLogger(CreateContentsApiHandler.class));
        this.dynamoDBClient = dynamoDBClient;
        this.s3Client = s3Client;
    }


    /**
     * Implements the main logic of the handler. Any exception thrown by this method will be handled by {@link
     * RestRequestHandler#handleExpectedException} method.
     *
     * @param request     The input object to the method. Usually a deserialized json.
     * @param requestInfo Request headers and path.
     * @param context     the ApiGateway context.
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
        if (StringUtils.isNotEmpty(contentsDocument.getIsbn())) {

            try {
                s3Client.handleFiles(contentsDocument);
                logger.error("This is my IndexDocument to index: " + contentsDocument.toString());
                dynamoDBClient.createContents(contentsDocument);
                String createContents = dynamoDBClient.getContents(contentsDocument.getIsbn());
                gatewayResponse.setBody(createContents);
                gatewayResponse.setStatusCode(HttpStatus.SC_CREATED);
            } catch (IOException e) {
                String msg = ERROR_STORING_FILE + e.getMessage();
                logger.error(msg, e);
                gatewayResponse.setErrorBody(msg);
                gatewayResponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            }

        } else {
            logger.error(COULD_NOT_INDEX_RECORD_PROVIDED + contentsDocument.toString());
            gatewayResponse.setErrorBody(COULD_NOT_INDEX_RECORD_PROVIDED + contentsDocument.toString());
            gatewayResponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
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
