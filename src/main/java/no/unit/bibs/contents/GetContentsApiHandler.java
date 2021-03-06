package no.unit.bibs.contents;

import com.amazonaws.services.lambda.runtime.Context;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.exceptions.commonexceptions.NotFoundException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.handlers.RestRequestHandler;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;

public class GetContentsApiHandler extends ApiGatewayHandler<Void, GatewayResponse> {

    public static final String ISBN = "isbn";
    private final DynamoDBClient dynamoDBClient;

    @JacocoGenerated
    public GetContentsApiHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public GetContentsApiHandler(Environment environment)  {
        this(environment, new DynamoDBClient(environment));
    }

    public GetContentsApiHandler(Environment environment, DynamoDBClient dynamoDBClient) {
        super(Void.class, environment, LoggerFactory.getLogger(GetContentsApiHandler.class));
        this.dynamoDBClient = dynamoDBClient;
    }


    /**
     * Implements the main logic of the handler. Any exception thrown by this method will be handled by {@link
     * RestRequestHandler#handleExpectedException} method.
     *
     * @param input       The input object to the method. Usually a deserialized json.
     * @param requestInfo Request headers and path.
     * @param context     the ApiGateway context.
     * @return the Response body that is going to be serialized in json
     * @throws ApiGatewayException all exceptions are caught by writeFailure and mapped to error codes through the
     *                             method {@link RestRequestHandler#getFailureStatusCode}
     */
    @Override
    protected GatewayResponse processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {
        String isbn = requestInfo.getQueryParameters().get(ISBN);
        GatewayResponse gatewayResponse = new GatewayResponse(environment);
        try {
            String contents = dynamoDBClient.getContents(isbn);
            gatewayResponse.setStatusCode(HttpStatus.SC_OK);
            gatewayResponse.setBody(contents);
        } catch (NotFoundException e) {
            gatewayResponse.setErrorBody(e.getMessage());
            gatewayResponse.setStatusCode(HttpStatus.SC_NOT_FOUND);
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
    protected Integer getSuccessStatusCode(Void input, GatewayResponse output) {
        return output.getStatusCode();
    }
}
