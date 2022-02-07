package no.unit.bibs.contents;

import static java.util.Objects.isNull;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.RestRequestHandler;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.GatewayResponseSerializingException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;

public class CreateContentsApiHandler extends ApiGatewayHandler<ContentsRequest, ContentsDocument> {

    public static final String NO_PARAMETERS_GIVEN_TO_HANDLER = "No parameters given to CreateContentsApiHandler";
    public static final String COULD_NOT_INDEX_RECORD_PROVIDED = "Could not persist provided contents. ";

    private final DynamoDBClient dynamoDBClient;
    private final S3Client s3Client;
    private final transient Logger logger = LoggerFactory.getLogger(CreateContentsApiHandler.class);

    @JacocoGenerated
    public CreateContentsApiHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public CreateContentsApiHandler(Environment environment) {
        this(environment, new DynamoDBClient(environment), new S3Client(environment));
    }


    /**
     * Constructor for injecting used in testing.
     * @param environment environment
     * @param dynamoDBClient dynamoDBclient
     * @param s3Client s3Client
     */
    public CreateContentsApiHandler(Environment environment, DynamoDBClient dynamoDBClient, S3Client s3Client) {
        super(ContentsRequest.class, environment);
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
    protected ContentsDocument processInput(ContentsRequest request, RequestInfo requestInfo,
                                           Context context) throws ApiGatewayException {
        if (isNull(request)) {
            throw new BadRequestException(NO_PARAMETERS_GIVEN_TO_HANDLER);
        }
        ContentsDocument contentsDocument = request.getContents();
        logger.error("json input looks like that :" + contentsDocument.toString());
        if (contentsDocument.isValid()) {
            s3Client.handleFiles(contentsDocument);
            dynamoDBClient.createContents(contentsDocument);
            String createContents = dynamoDBClient.getContents(contentsDocument.getIsbn());
            try {
                ContentsDocument response = dtoObjectMapper.readValue(createContents, ContentsDocument.class);
                return response;
            } catch (JsonProcessingException e) {
                throw new GatewayResponseSerializingException(e);
            }
        } else {
            logger.error(COULD_NOT_INDEX_RECORD_PROVIDED + contentsDocument);
            throw new BadRequestException(COULD_NOT_INDEX_RECORD_PROVIDED + contentsDocument);
        }
    }

    @Override
    protected Integer getSuccessStatusCode(ContentsRequest input, ContentsDocument output) {
        return HttpURLConnection.HTTP_CREATED;
    }


}
