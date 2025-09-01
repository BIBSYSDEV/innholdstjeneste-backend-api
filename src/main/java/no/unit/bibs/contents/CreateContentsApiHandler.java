package no.unit.bibs.contents;

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

import java.net.HttpURLConnection;

import static java.util.Objects.isNull;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;

public class CreateContentsApiHandler extends ApiGatewayHandler<ContentsRequest, ContentsDocument> {

    public static final String NO_PARAMETERS_GIVEN_TO_HANDLER = "No parameters given to CreateContentsApiHandler";
    public static final String COULD_NOT_INDEX_RECORD_PROVIDED = "Could not persist provided contents. ";

    private final DBClient dynamoDBClient;
    private final StorageClient storageClient;


    @JacocoGenerated
    @SuppressWarnings("unused")
    public CreateContentsApiHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public CreateContentsApiHandler(Environment environment) {
        this(environment, new DBClient(environment), new StorageClient(environment));
    }


    /**
     * Constructor for injecting used in testing.
     *
     * @param environment    environment
     * @param dynamoDBClient dynamoDBClient
     * @param storageClient  storageClient
     */
    public CreateContentsApiHandler(Environment environment, DBClient dynamoDBClient,
                                    StorageClient storageClient) {
        super(ContentsRequest.class, environment);
        this.dynamoDBClient = dynamoDBClient;
        this.storageClient = storageClient;
    }


    @Override
    protected void validateRequest(ContentsRequest input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        if (isNull(input)) {
            throw new BadRequestException(NO_PARAMETERS_GIVEN_TO_HANDLER);
        }
        if (!input.getContents().isValid()) {
            throw new BadRequestException(COULD_NOT_INDEX_RECORD_PROVIDED + input.getContents());
        }
    }

    /**
     * Implements the main logic of the handler. Any exception thrown by this method will be handled by {@link
     * RestRequestHandler#handleExpectedException} method.
     *
     * @param input     The input object to the method. Usually a deserialized JSON.
     * @param requestInfo Request headers and path.
     * @param context     the ApiGateway context.
     * @return the Response body that is going to be serialized in JSON.
     * @throws ApiGatewayException all exceptions are caught by writeFailure and mapped to error codes through the
     *                             method {@link RestRequestHandler#getFailureStatusCode}
     */
    @Override
    protected ContentsDocument processInput(ContentsRequest input, RequestInfo requestInfo,
                                            Context context) throws ApiGatewayException {
        var contentsDocument = input.getContents();
        try {
            storageClient.handleFiles(contentsDocument);
            dynamoDBClient.createContents(contentsDocument);
            var createContents = dynamoDBClient.getContents(contentsDocument.getIsbn());
            return dtoObjectMapper.readValue(createContents, ContentsDocument.class);

        } catch (JsonProcessingException e) {
            throw new GatewayResponseSerializingException(e);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(COULD_NOT_INDEX_RECORD_PROVIDED);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    @Override
    protected Integer getSuccessStatusCode(ContentsRequest input, ContentsDocument output) {
        return HttpURLConnection.HTTP_CREATED;
    }


}
