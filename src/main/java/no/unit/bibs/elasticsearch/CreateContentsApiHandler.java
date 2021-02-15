package no.unit.bibs.elasticsearch;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.unit.bibs.elasticsearch.exception.ImportException;
import no.unit.bibs.elasticsearch.exception.SearchException;
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
    private final ElasticSearchHighLevelRestClient elasticSearchClient;

    @JacocoGenerated
    public CreateContentsApiHandler() {
        this(new Environment());
    }

    public CreateContentsApiHandler(Environment environment) {
        this(environment, new ElasticSearchHighLevelRestClient(environment));
    }

    public CreateContentsApiHandler(Environment environment, ElasticSearchHighLevelRestClient elasticSearchClient) {
        super(CreateContentsRequest.class, environment, LoggerFactory.getLogger(CreateContentsApiHandler.class));
        this.elasticSearchClient = elasticSearchClient;
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
        logger.debug("json input looks like that :" + json);
        Optional<IndexDocument> indexDocument = fromJsonString(json);
        if (indexDocument.isPresent()) {
            addDocumentToIndex(indexDocument.get());
        } else {
            logger.error(COULD_NOT_INDEX_RECORD_PROVIDED + json);
        }
        return new CreateContentsResponse(CHECK_LOG_FOR_DETAILS_MESSAGE, request, HttpStatus.SC_CREATED,
                Instant.now());
    }


    private Optional<IndexDocument> fromJsonString(String line) {
        try {
            JsonNode node = mapper.readTree(line);
            var indexDocument = IndexDocumentGenerator.fromJsonNode(node);
            return Optional.of(indexDocument);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    private void addDocumentToIndex(IndexDocument document) {
        try {
            elasticSearchClient.addDocumentToIndex(document);
        } catch (SearchException e) {
            logger.error(ERROR_ADDING_DOCUMENT_SEARCH_INDEX, document.getId(), e);
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
