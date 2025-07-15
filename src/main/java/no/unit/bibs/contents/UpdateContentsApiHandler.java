package no.unit.bibs.contents;

import static java.util.Objects.isNull;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.StringUtils.isEmpty;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.bibs.contents.exception.CommunicationException;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.RestRequestHandler;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.ConflictException;
import nva.commons.apigateway.exceptions.GatewayResponseSerializingException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;


public class UpdateContentsApiHandler extends ApiGatewayHandler<ContentsRequest, ContentsDocument> {

    public static final String NO_PARAMETERS_GIVEN_TO_HANDLER = "No parameters given to UpdateContentsApiHandler";
    public static final String CONTENTS_CREATED = "contents created";
    public static final String CONTENTS_UPDATED = "contents updated";
    public static final String FAILED_AFTER_PERSISTING = "failed after persisting: ";
    public static final int FOURTH_OF_A_SECOND = 250;

    private final DBClient dynamoDBClient;
    private final StorageClient storageClient;
    private final transient Logger logger = LoggerFactory.getLogger(UpdateContentsApiHandler.class);

    @JacocoGenerated
    public UpdateContentsApiHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public UpdateContentsApiHandler(Environment environment) {
        this(environment, new DBClient(environment), new StorageClient(environment));
    }

    /**
     * Constructor for injecting used in testing.
     *
     * @param environment    environment
     * @param dynamoDBClient dynamoDBclient
     * @param storageClient  storageClient
     */
    public UpdateContentsApiHandler(Environment environment, DBClient dynamoDBClient,
                                    StorageClient storageClient) {
        super(ContentsRequest.class, environment);
        this.dynamoDBClient = dynamoDBClient;
        this.storageClient = storageClient;
    }


    @Override
    protected void validateRequest(ContentsRequest input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        if (isNull(input)) {
            throw new BadRequestException(NO_PARAMETERS_GIVEN_TO_HANDLER);
        }
        if (!input.getContents().isValid()) {
            throw new BadRequestException("Document is not valid: " + input.getContents());
        }
    }

    /**
     * Implements the main logic of the handler. Any exception thrown by this method will be handled by {@link
     * RestRequestHandler#handleExpectedException} method.
     *
     * @param input     The input object to the method. Usually a deserialized json.
     * @param requestInfo Request headers and path.
     * @param context     the ApiGateway context.
     * @return the Response body that is going to be serialized in json
     * @throws ApiGatewayException all exceptions are caught by writeFailure and mapped to error codes through the
     *                             method {@link RestRequestHandler#getFailureStatusCode}
     */
    @Override
    protected ContentsDocument processInput(ContentsRequest input, RequestInfo requestInfo,
                                            Context context) throws ApiGatewayException {

        var contentsDocument = input.getContents();
        storageClient.handleFiles(contentsDocument);
        try {
            var contents = dynamoDBClient.getContents(contentsDocument.getIsbn());
            if (isEmpty(contents)) {
                return createContents(contentsDocument);
            } else {
                return updateContents(contentsDocument);
            }
        } catch (NotFoundException e) {
            return createContents(contentsDocument);
        } catch (Exception e) {
            var msg = FAILED_AFTER_PERSISTING + e.getMessage();
            logger.error(msg, e);
            throw new ConflictException(msg);
        }
    }

    private ContentsDocument createContents(ContentsDocument contentsDocument) throws CommunicationException,
            NotFoundException, GatewayResponseSerializingException {

        try {
            dynamoDBClient.createContents(contentsDocument);
            this.waitAMoment(FOURTH_OF_A_SECOND);
            var createdContents = dynamoDBClient.getContents(contentsDocument.getIsbn());
            if (isEmpty(createdContents)) {
                throw new NotFoundException("Contents with ISBN <" + contentsDocument.getIsbn() + "> not found after creation");
            }
            logger.info(CONTENTS_CREATED);
            return dtoObjectMapper.readValue(createdContents, ContentsDocument.class);
        } catch (JsonProcessingException ex) {
            throw new GatewayResponseSerializingException(ex);
        }
    }

    private ContentsDocument updateContents(ContentsDocument contentsDocument) throws CommunicationException,
            GatewayResponseSerializingException, NotFoundException {
        dynamoDBClient.updateContents(contentsDocument);
        this.waitAMoment(FOURTH_OF_A_SECOND);
        var updatedContents = dynamoDBClient.getContents(contentsDocument.getIsbn());
        try {
            logger.info(CONTENTS_UPDATED);
            return dtoObjectMapper.readValue(updatedContents, ContentsDocument.class);
        } catch (JsonProcessingException ex) {
            throw new GatewayResponseSerializingException(ex);
        }
    }

    @JacocoGenerated
    private void waitAMoment(int millisec) {
        try {
            Thread.sleep(millisec);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }


    @Override
    protected Integer getSuccessStatusCode(ContentsRequest input, ContentsDocument output) {
        return HttpURLConnection.HTTP_CREATED;
    }
}
