package no.unit.bibs.contents;

import com.amazonaws.services.lambda.runtime.Context;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.handlers.RestRequestHandler;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class GetContentFileApiHandler extends ApiGatewayHandler<Void, GatewayResponse> {

    public static final String BUCKET_URL_TEMPLATE = "https://%s.s3.%s.amazonaws.com/files/%s/%s/%s/%s/%s";
    public static final String PATH_PARAMETER_TYPE = "type";
    public static final String PATH_PARAMETER_SUBTYPE = "subtype";
    public static final String PATH_PARAMETER_FIRST_LINK_PART = "firstLinkPart";
    public static final String PATH_PARAMETER_SECOND_LINK_PART = "secondLinkPart";
    public static final String PATH_PARAMETER_FILENAME = "filename";
    public static final String BUCKET_NAME = "BUCKET_NAME";
    public static final String AWS_REGION = "AWS_REGION";

    @JacocoGenerated
    public GetContentFileApiHandler() {
        this(new Environment());
    }

    public GetContentFileApiHandler(Environment environment) {
        super(Void.class, environment, LoggerFactory.getLogger(GetContentFileApiHandler.class));
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
    protected GatewayResponse processInput(Void input, RequestInfo requestInfo, Context context) {

        Map<String, String> pathParameters = requestInfo.getPathParameters();
        String type = pathParameters.get(PATH_PARAMETER_TYPE);
        String subtype = pathParameters.get(PATH_PARAMETER_SUBTYPE);
        String firstLinkPart = pathParameters.get(PATH_PARAMETER_FIRST_LINK_PART);
        String secondLinkPart = pathParameters.get(PATH_PARAMETER_SECOND_LINK_PART);
        String filename = pathParameters.get(PATH_PARAMETER_FILENAME);


        String contentFileUrl = String.format(BUCKET_URL_TEMPLATE, environment.readEnv(BUCKET_NAME),
        environment.readEnv(AWS_REGION), type, subtype, firstLinkPart, secondLinkPart, filename);
        GatewayResponse gatewayResponse = new GatewayResponse(environment, contentFileUrl);
        gatewayResponse.setStatusCode(HttpStatus.SC_MOVED_PERMANENTLY);

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
