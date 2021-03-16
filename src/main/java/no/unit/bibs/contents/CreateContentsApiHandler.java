package no.unit.bibs.contents;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.unit.bibs.contents.exception.CommunicationException;
import no.unit.bibs.contents.exception.ImportException;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.handlers.RestRequestHandler;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.JsonUtils;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;

import static java.util.Objects.isNull;

public class CreateContentsApiHandler extends ApiGatewayHandler<CreateContentsRequest, CreateContentsResponse> {

    public static final String NO_PARAMETERS_GIVEN_TO_HANDLER = "No parameters given to CreateContentsApiHandler";
    public static final String CHECK_LOG_FOR_DETAILS_MESSAGE = "DataImport created, check log for details";
    public static final String ERROR_ADDING_DOCUMENT_SEARCH_INDEX = "Error adding document with id={} to searchIndex";
    public static final String COULD_NOT_INDEX_RECORD_PROVIDED = "Could not index record provided. ";


    private static final ObjectMapper mapper = JsonUtils.objectMapper;
    private final DynamoDBClient dynamoDBClient;

    @JacocoGenerated
    public CreateContentsApiHandler() throws CommunicationException {
        this(new Environment());
    }

    public CreateContentsApiHandler(Environment environment) throws CommunicationException {
        this(environment, new DynamoDBClient(environment));
    }

    public CreateContentsApiHandler(Environment environment, DynamoDBClient dynamoDBClient) {
        super(CreateContentsRequest.class, environment, LoggerFactory.getLogger(CreateContentsApiHandler.class));
        this.dynamoDBClient = dynamoDBClient;
    }


    /**
     * Implements the main logic of the handler. Any exception thrown by this method will be handled by {@link
     * RestRequestHandler#handleExpectedException} method.
     *
     * @param request       The input object to the method. Usually a deserialized json.
     * @param requestInfo Request headers and path.
     * @param context     the ApiGateway context.ucket
     * @return the Response body that is going to be serialized in json
     * @throws ApiGatewayException all exceptions are caught by writeFailure and mapped to error codes through the
     *                             method {@link RestRequestHandler#getFailureStatusCode}
     */
    @Override
    protected CreateContentsResponse processInput(CreateContentsRequest request, RequestInfo requestInfo,
                                                  Context context) throws ApiGatewayException {
        if (isNull(request)) {
            throw new ImportException(NO_PARAMETERS_GIVEN_TO_HANDLER);
        }
        String json = request.getContents();
        logger.error("json input looks like that :" + json);
        Optional<ContentsDocument> indexDocument = fromJsonString(json);
        if (indexDocument.isPresent()) {
            logger.error("This is my IndexDocument to index: " + indexDocument.toString());
            addDocumentToIndex(indexDocument.get());
        } else {
            logger.error(COULD_NOT_INDEX_RECORD_PROVIDED + json);
        }
        return new CreateContentsResponse(CHECK_LOG_FOR_DETAILS_MESSAGE, request, HttpStatus.SC_CREATED,
                Instant.now());
    }

    private Optional<ContentsDocument> fromJsonString(String line) {
        try {
            ContentsDocument contentsDocument = mapper.readValue(line, ContentsDocument.class);
            return Optional.of(contentsDocument);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    private void addDocumentToIndex(ContentsDocument document) {
        try {
            dynamoDBClient.addContents(document);
        } catch (CommunicationException e) {
            e.printStackTrace();
        }
    }


    /**
     * Define the success status code.
     *
     * @param input  The request input.
     * @param output The response output
     * @return the success status code.
     */
    @Override
    protected Integer getSuccessStatusCode(CreateContentsRequest input, CreateContentsResponse output) {
        return output.getStatusCode();
    }
}
