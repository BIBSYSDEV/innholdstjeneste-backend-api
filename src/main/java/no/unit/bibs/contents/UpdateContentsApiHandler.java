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

import java.net.HttpURLConnection;


public class UpdateContentsApiHandler extends ApiGatewayHandler<ContentsRequest, ContentsDocument> {

    public static final String NO_PARAMETERS_GIVEN_TO_HANDLER = "No parameters given to UpdateContentsApiHandler";
    public static final String FAILED_AFTER_PERSISTING = "failed after persisting: ";
    public static final int FOURTH_OF_A_SECOND = 250;

    private final DBClient dbClient;
    private final StorageClient storageClient;

    @JacocoGenerated
    @SuppressWarnings("unused")
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
     * @param dbClient        dbClient
     * @param storageClient  storageClient
     */
    public UpdateContentsApiHandler(Environment environment, DBClient dbClient,
                                    StorageClient storageClient) {
        super(ContentsRequest.class, environment);
        this.dbClient = dbClient;
        this.storageClient = storageClient;
    }


    @Override
    protected void validateRequest(ContentsRequest input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

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
        try {
            storageClient.handleFiles(contentsDocument);
            var contents = dbClient.getContents(contentsDocument.getIsbn());
            if (isEmpty(contents)) {
                return createContents(contentsDocument);
            } else {
                return updateContents(contentsDocument);
            }
        } catch (NotFoundException e) {
            return createContents(contentsDocument);
        } catch (Exception e) {
            throw new ConflictException(FAILED_AFTER_PERSISTING + e.getMessage());
        }
    }

    private ContentsDocument createContents(ContentsDocument contentsDocument) throws CommunicationException,
            NotFoundException, GatewayResponseSerializingException {

        try {
            dbClient.createContents(contentsDocument);
            this.waitAMoment(FOURTH_OF_A_SECOND);
            var createdContents = dbClient.getContents(contentsDocument.getIsbn());
            if (isEmpty(createdContents)) {
                throw new NotFoundException("Contents with ISBN <" + contentsDocument.getIsbn()
                                            + "> not found after creation");
            }
            return dtoObjectMapper.readValue(createdContents, ContentsDocument.class);
        } catch (JsonProcessingException ex) {
            throw new GatewayResponseSerializingException(ex);
        }
    }

    private ContentsDocument updateContents(ContentsDocument contentsDocument) throws CommunicationException,
            GatewayResponseSerializingException, NotFoundException {
        try {
            dbClient.updateContents(contentsDocument);
            this.waitAMoment(FOURTH_OF_A_SECOND);
            var updatedContents = dbClient.getContents(contentsDocument.getIsbn());
            return dtoObjectMapper.readValue(updatedContents, ContentsDocument.class);
        } catch (JsonProcessingException ex) {
            throw new GatewayResponseSerializingException(ex);
        }
    }

    @SuppressWarnings("PMD.DoNotUseThreads")
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
