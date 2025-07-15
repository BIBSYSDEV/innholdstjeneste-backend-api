package no.unit.bibs.contents;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.GatewayResponseSerializingException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;

public class GetContentsApiHandler extends ApiGatewayHandler<Void, ContentsDocument> {

    public static final String ISBN = "isbn";
    public static final String MISSING_REQUIRED_QUERY_PARAMETER = "Missing required query parameter: ";
    private final DBClient dbClient;

    @JacocoGenerated
    public GetContentsApiHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public GetContentsApiHandler(Environment environment) {
        this(environment, new DBClient(environment));
    }

    public GetContentsApiHandler(Environment environment, DBClient dbClient) {
        super(Void.class, environment);
        this.dbClient = dbClient;
    }

    @Override
    protected void validateRequest(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        if (requestInfo.getQueryParameter(ISBN) == null) {
            throw new BadRequestException(MISSING_REQUIRED_QUERY_PARAMETER + ISBN);
        }
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
    protected ContentsDocument processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {
        var isbn = requestInfo.getQueryParameter(ISBN);
        var contents = dbClient.getContents(isbn);
        try {
             return dtoObjectMapper.readValue(contents, ContentsDocument.class);
        } catch (JsonProcessingException e) {
            throw new GatewayResponseSerializingException(e);
        }
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, ContentsDocument output) {
        return HttpURLConnection.HTTP_OK;
    }

}
