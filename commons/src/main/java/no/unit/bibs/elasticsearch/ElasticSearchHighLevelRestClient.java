package no.unit.bibs.elasticsearch;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.utils.Environment;
import nva.commons.utils.JsonUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class ElasticSearchHighLevelRestClient {


    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchHighLevelRestClient.class);

    private static final String SERVICE_NAME = "es";
    public static final String ELASTICSEARCH_ENDPOINT_INDEX_KEY = "ELASTICSEARCH_ENDPOINT_INDEX";
    public static final String ELASTICSEARCH_ENDPOINT_ADDRESS_KEY = "ELASTICSEARCH_ENDPOINT_ADDRESS";
    public static final String ELASTICSEARCH_ENDPOINT_API_SCHEME_KEY = "ELASTICSEARCH_ENDPOINT_API_SCHEME";
    public static final String ELASTICSEARCH_ENDPOINT_REGION_KEY = "ELASTICSEARCH_REGION";


    public static final String INITIAL_LOG_MESSAGE = "using Elasticsearch endpoint {} and index {}";
    public static final String SOURCE_JSON_POINTER = "/_source";
    public static final String TOTAL_JSON_POINTER = "/hits/total/value";
    public static final String TOOK_JSON_POINTER = "/took";
    public static final String HITS_JSON_POINTER = "/hits/hits";
    public static final String DOCUMENT_WITH_ID_WAS_NOT_FOUND_IN_ELASTICSEARCH
            = "Document with id={} was not found in elasticsearch";

    private static final ObjectMapper mapper = JsonUtils.objectMapper;
    private final String elasticSearchEndpointAddress;


    private final String elasticSearchRegion;
    private static final AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();
    private final String elasticSearchEndpointIndex;
    private final RestHighLevelClient elasticSearchClient;

    public static final URI DEFAULT_SEARCH_CONTEXT = URI.create("https://api.bibs.unit.no/contents/search");

    /**
     * Creates a new ElasticSearchRestClient.
     *
     * @param environment Environment with properties
     */
    public ElasticSearchHighLevelRestClient(Environment environment) {
        elasticSearchEndpointAddress = environment.readEnv(ELASTICSEARCH_ENDPOINT_ADDRESS_KEY);
        elasticSearchEndpointIndex = environment.readEnv(ELASTICSEARCH_ENDPOINT_INDEX_KEY);
        elasticSearchRegion = environment.readEnv(ELASTICSEARCH_ENDPOINT_REGION_KEY);
        elasticSearchClient = createElasticsearchClientWithInterceptor(
                elasticSearchRegion,
                elasticSearchEndpointAddress);
        logger.info(INITIAL_LOG_MESSAGE, elasticSearchEndpointAddress, elasticSearchEndpointIndex);
    }

    /**
     * Creates a new ElasticSearchRestClient.
     *
     * @param environment Environment with properties
     * @param elasticSearchClient client to use for access to ElasticSearch
     */
    public ElasticSearchHighLevelRestClient(Environment environment, RestHighLevelClient elasticSearchClient) {
        elasticSearchEndpointAddress = environment.readEnv(ELASTICSEARCH_ENDPOINT_ADDRESS_KEY);
        elasticSearchEndpointIndex = environment.readEnv(ELASTICSEARCH_ENDPOINT_INDEX_KEY);
        elasticSearchRegion = environment.readEnv(ELASTICSEARCH_ENDPOINT_REGION_KEY);
        this.elasticSearchClient = elasticSearchClient;
        logger.info(INITIAL_LOG_MESSAGE, elasticSearchEndpointAddress, elasticSearchEndpointIndex);
    }

    /**
     * Searches for an term or index:term in elasticsearch index.
     * @param term search argument
     * @param results number of results
     * @throws ApiGatewayException thrown when uri is misconfigured, service i not available or interrupted
     */
    public QueryContentsResponse searchSingleTerm(String term,
                                             int results,
                                             int from,
                                             String orderBy,
                                             SortOrder sortOrder) throws ApiGatewayException {
        try {
            SearchResponse searchResponse = doSearch(term, results, from, orderBy, sortOrder);
            return toSearchResourcesResponse(searchResponse.toString());
        } catch (Exception e) {
            throw new SearchException(e.getMessage(), e);
        }
    }

    private SearchResponse doSearch(String term,
                                    int results,
                                    int from,
                                    String orderBy,
                                    SortOrder sortOrder) throws IOException {
        return elasticSearchClient.search(getSearchRequest(term,
                results,
                from,
                orderBy,
                sortOrder), RequestOptions.DEFAULT);
    }

    private SearchRequest getSearchRequest(String term, int results, int from, String orderBy, SortOrder sortOrder) {
        final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
            .query(QueryBuilders.queryStringQuery(term))
            .sort(orderBy, sortOrder)
            .from(from)
            .size(results);
        return new SearchRequest(elasticSearchEndpointIndex).source(sourceBuilder);
    }

    /**
     * Adds or insert a document to an elasticsearch index.
     * @param document the document to be inserted
     * @throws SearchException when something goes wrong
     * */
    public void addDocumentToIndex(IndexDocument document) throws SearchException {
        try {
            doUpsert(document);
        } catch (Exception e) {
            throw new SearchException(e.getMessage(), e);
        }
    }

    private void doUpsert(IndexDocument document) throws IOException {
        elasticSearchClient.update(getUpdateRequest(document), RequestOptions.DEFAULT);
    }

    private UpdateRequest getUpdateRequest(IndexDocument document) throws JsonProcessingException {
        IndexRequest indexRequest = new IndexRequest(elasticSearchEndpointIndex)
                .source(document.toJsonString(), XContentType.JSON);
        return new UpdateRequest(elasticSearchEndpointIndex,  document.getId().toString())
            .upsert(indexRequest)
            .doc(indexRequest);
    }

    /**
     * Removes an document from Elasticsearch index.
     * @param identifier og document
     * @throws SearchException when
     */
    public void removeDocumentFromIndex(String identifier) throws SearchException {
        try {
            doDelete(identifier);
        } catch (Exception e) {
            throw new SearchException(e.getMessage(), e);
        }
    }

    private void doDelete(String identifier) throws IOException {
        DeleteResponse deleteResponse = elasticSearchClient
                .delete(new DeleteRequest(elasticSearchEndpointIndex, identifier), RequestOptions.DEFAULT);
        if (deleteResponse.getResult() == DocWriteResponse.Result.NOT_FOUND) {
            logger.warn(DOCUMENT_WITH_ID_WAS_NOT_FOUND_IN_ELASTICSEARCH, identifier);
        }
    }

    private QueryContentsResponse toSearchResourcesResponse(String body) throws JsonProcessingException {
        JsonNode values = mapper.readTree(body);

        List<JsonNode> sourceList = extractSourceList(values);
        int total = intFromNode(values, TOTAL_JSON_POINTER);
        int took =  intFromNode(values, TOOK_JSON_POINTER);

        return new QueryContentsResponse.Builder()
                .withContext(DEFAULT_SEARCH_CONTEXT)
                .withTook(took)
                .withTotal(total)
                .withHits(sourceList)
                .build();
    }

    private List<JsonNode> extractSourceList(JsonNode record) {
        return toStream(record.at(HITS_JSON_POINTER))
                .map(this::extractSourceStripped)
                .collect(Collectors.toList());
    }

    private static int intFromNode(JsonNode jsonNode, String jsonPointer) {
        JsonNode json = jsonNode.at(jsonPointer);
        return isPopulated(json) ? json.asInt() : 0;
    }

    private static boolean isPopulated(JsonNode json) {
        return !json.isNull() && !json.asText().isBlank();
    }

    private JsonNode extractSourceStripped(JsonNode record) {
        return record.at(SOURCE_JSON_POINTER);
    }

    private Stream<JsonNode> toStream(JsonNode node) {
        return StreamSupport.stream(node.spliterator(), false);
    }

    protected final RestHighLevelClient createElasticsearchClientWithInterceptor(String region,
                                                                                 String elasticSearchEndpoint) {
        AWS4Signer signer = getAws4Signer(ElasticSearchHighLevelRestClient.SERVICE_NAME, region);
        HttpRequestInterceptor interceptor =
                new AWSRequestSigningApacheInterceptor(ElasticSearchHighLevelRestClient.SERVICE_NAME,
                        signer,
                        credentialsProvider);

        return new RestHighLevelClient(RestClient.builder(HttpHost.create(elasticSearchEndpoint))
                .setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
    }

    private AWS4Signer getAws4Signer(String serviceName, String region) {
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(serviceName);
        signer.setRegionName(region);
        return signer;
    }

}
