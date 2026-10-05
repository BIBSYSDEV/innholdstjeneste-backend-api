package no.unit.bibs.contents;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.GatewayResponseSerializingException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;

public class GetContentsApiHandler extends ApiGatewayHandler<Void, ContentsDocument> {

    private static final Logger logger = LoggerFactory.getLogger(GetContentsApiHandler.class);

    public static final String ISBN = "isbn";
    private static final String GETTING_CONTENT_FOR_ISBN = "Getting content for isbn {}";
    private static final String CONTENT_FOUND = "Content found: {}";
    private static final String COULD_NOT_DESERIALIZE_CONTENT = "Could not deserialize content for isbn {}. Error: {}";

    private final DBClient dbClient;

    @JacocoGenerated
    @SuppressWarnings("unused")
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
        requestInfo.getQueryParameter(ISBN);
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
        logger.info(GETTING_CONTENT_FOR_ISBN, isbn);
        var contents = dbClient.getContents(isbn);
        logger.info(CONTENT_FOUND, contents);
        try {
            return dtoObjectMapper.readValue(contents, ContentsDocument.class);
        } catch (JsonProcessingException e) {
            logger.error(COULD_NOT_DESERIALIZE_CONTENT, isbn, e.getMessage());
            throw new GatewayResponseSerializingException(e);
        }
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, ContentsDocument output) {
        return HttpURLConnection.HTTP_OK;
    }

}
