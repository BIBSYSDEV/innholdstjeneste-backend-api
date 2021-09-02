package no.unit.bibs.contents;

import com.amazonaws.services.lambda.runtime.Context;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetContentsApiHandler extends ApiGatewayHandler<Void, GatewayResponse> {

    public static final String ISBN = "isbn";
    private final DynamoDBClient dynamoDBClient;
    private transient Logger logger = LoggerFactory.getLogger(GetContentsApiHandler.class);

    @JacocoGenerated
    public GetContentsApiHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public GetContentsApiHandler(Environment environment)  {
        this(environment, new DynamoDBClient(environment));
    }

    public GetContentsApiHandler(Environment environment, DynamoDBClient dynamoDBClient) {
        super(Void.class, environment);
        this.dynamoDBClient = dynamoDBClient;
    }


    /**
     * Implements the main logic of the handler. Any exception thrown by this method will be handled by method.
     *
     * @param input       The input object to the method. Usually a deserialized json.
     * @param requestInfo Request headers and path.
     * @param context     the ApiGateway context.
     * @return the Response body that is going to be serialized in json
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
