package no.unit.bibs.elasticsearch;

import com.amazonaws.services.lambda.runtime.Context;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.handlers.RestRequestHandler;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;

public class QueryContentsApiHandler extends ApiGatewayHandler<Void, QueryContentsResponse> {

    private final ElasticSearchHighLevelRestClient elasticSearchClient;

    @JacocoGenerated
    public QueryContentsApiHandler() {
        this(new Environment());
    }

    public QueryContentsApiHandler(Environment environment) {
        this(environment, new ElasticSearchHighLevelRestClient(environment));
    }

    public QueryContentsApiHandler(Environment environment, ElasticSearchHighLevelRestClient elasticSearchClient) {
        super(Void.class, environment, LoggerFactory.getLogger(QueryContentsApiHandler.class));
        this.elasticSearchClient = elasticSearchClient;
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
    protected QueryContentsResponse processInput(Void input,
                                                 RequestInfo requestInfo,
                                                 Context context) throws ApiGatewayException {

        String searchTerm = RequestUtil.getSearchTerm(requestInfo);
        int results = RequestUtil.getResults(requestInfo);
        int from = RequestUtil.getFrom(requestInfo);
        return elasticSearchClient.searchSingleTerm(searchTerm, results, from);
    }


    /**
     * Define the success status code.
     *
     * @param input  The request input.
     * @param output The response output
     * @return the success status code.
     */
    @Override
    protected Integer getSuccessStatusCode(Void input, QueryContentsResponse output) {
        return HttpStatus.SC_OK;
    }
}
