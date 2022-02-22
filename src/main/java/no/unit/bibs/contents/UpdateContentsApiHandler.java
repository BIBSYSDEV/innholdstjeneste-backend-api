package no.unit.bibs.contents;

import static java.util.Objects.isNull;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.bibs.contents.exception.CommunicationException;
import no.unit.bibs.contents.exception.ParameterException;
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
import nva.commons.core.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;


public class UpdateContentsApiHandler extends ApiGatewayHandler<ContentsRequest, ContentsDocument> {

    public static final String NO_PARAMETERS_GIVEN_TO_HANDLER = "No parameters given to UpdateContentsApiHandler";
    public static final String COULD_NOT_UPDATE_PROVIDED_CONTENTS = "Could not update provided contents. ";
    public static final String CONTENTS_CREATED = "contents created";
    public static final String CONTENTS_UPDATED = "contents updated";
    public static final String FAILED_AFTER_PERSISTING = "failed after persisting: ";
    public static final String ERROR_IN_UPDATE_FUNCTION = "error in update function: ";
    public static final String THIS_IS_MY_CONTENTS_DOCUMENT_TO_PERSIST = "This is my ContentsDocument to persist: ";
    public static final String JSON_INPUT_LOOKS_LIKE_THAT = "json input looks like that :";
    public static final int HALF_A_SECOND = 500;

    private final DynamoDBClient dynamoDBClient;
    private final S3Client s3Client;
    private final transient Logger logger = LoggerFactory.getLogger(UpdateContentsApiHandler.class);

    @JacocoGenerated
    public UpdateContentsApiHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public UpdateContentsApiHandler(Environment environment) {
        this(environment, new DynamoDBClient(environment), new S3Client(environment));
    }

    /**
     * Constructor for injecting used in testing.
     *
     * @param environment    environment
     * @param dynamoDBClient dynamoDBclient
     * @param s3Client       s3Client
     */
    public UpdateContentsApiHandler(Environment environment, DynamoDBClient dynamoDBClient, S3Client s3Client) {
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
            throw new ParameterException(NO_PARAMETERS_GIVEN_TO_HANDLER);
        }
        ContentsDocument contentsDocument = request.getContents();
        logger.info(JSON_INPUT_LOOKS_LIKE_THAT + contentsDocument.toString());

        if (contentsDocument.isValid()) {

            s3Client.handleFiles(contentsDocument);

            logger.debug(THIS_IS_MY_CONTENTS_DOCUMENT_TO_PERSIST + contentsDocument.toString());
            try {
                String contents = dynamoDBClient.getContents(contentsDocument.getIsbn());
                if (StringUtils.isEmpty(contents)) {
                    return createContents(contentsDocument);
                } else {
                    return updateContents(contentsDocument);
                }
            } catch (NotFoundException e) {
                return createContents(contentsDocument);
            } catch (Exception e) {
                String msg = FAILED_AFTER_PERSISTING + e.getMessage();
                logger.error(msg, e);
                throw new ConflictException(msg);
            }
        } else {
            logger.error(COULD_NOT_UPDATE_PROVIDED_CONTENTS + contentsDocument);
            throw new BadRequestException(COULD_NOT_UPDATE_PROVIDED_CONTENTS + contentsDocument);
        }
    }

    private ContentsDocument createContents(ContentsDocument contentsDocument) throws CommunicationException,
            NotFoundException, GatewayResponseSerializingException {
        dynamoDBClient.createContents(contentsDocument);
        this.waitAMoment(HALF_A_SECOND);
        String createdContents = dynamoDBClient.getContents(contentsDocument.getIsbn());
        logger.info(CONTENTS_CREATED);
        try {
            ContentsDocument contents = dtoObjectMapper.readValue(createdContents, ContentsDocument.class);
            return contents;
        } catch (JsonProcessingException ex) {
            throw new GatewayResponseSerializingException(ex);
        }
    }

    private ContentsDocument updateContents(ContentsDocument contentsDocument) throws CommunicationException,
            GatewayResponseSerializingException {
        String updatedContents = dynamoDBClient.updateContents(contentsDocument);
        logger.info(CONTENTS_UPDATED);
        try {
            ContentsDocument response = dtoObjectMapper.readValue(updatedContents, ContentsDocument.class);
            return response;
        } catch (JsonProcessingException e) {
            throw new GatewayResponseSerializingException(e);
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
