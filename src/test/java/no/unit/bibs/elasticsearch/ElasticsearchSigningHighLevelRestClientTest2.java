package no.unit.bibs.elasticsearch;

import no.unit.bibs.elasticsearch.exception.SearchException;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.utils.Environment;
import nva.commons.utils.IoUtils;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static no.unit.bibs.elasticsearch.ElasticSearchHighLevelRestClient.ELASTICSEARCH_ENDPOINT_ADDRESS_KEY;
import static no.unit.bibs.elasticsearch.ElasticSearchHighLevelRestClient.ELASTICSEARCH_ENDPOINT_API_SCHEME_KEY;
import static no.unit.bibs.elasticsearch.ElasticSearchHighLevelRestClient.ELASTICSEARCH_ENDPOINT_INDEX_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ElasticsearchSigningHighLevelRestClientTest2 {

    private static final String elasticSearchEndpoint = "http://localhost";
    public static final String SAMPLE_TERM = "SampleSearchTerm";
    private static final int SAMPLE_NUMBER_OF_RESULTS = 7;
    private static final String SAMPLE_JSON_RESPONSE = "{}";
    private static final int SAMPLE_FROM = 0;
    private static final String SAMPLE_ORDERBY = "orderByField";
    private static final String ELASTIC_SAMPLE_RESPONSE_FILE = "sample_elasticsearch_response.json";
    private static final int ELASTIC_ACTUAL_SAMPLE_NUMBER_OF_RESULTS = 2;
    public static final int MAX_RESULTS = 100;

    ElasticSearchHighLevelRestClient elasticSearchRestClient;
    private Environment environment;

    private void initEnvironment() {
        when(environment.readEnv(ELASTICSEARCH_ENDPOINT_ADDRESS_KEY)).thenReturn(elasticSearchEndpoint);
        when(environment.readEnv(ELASTICSEARCH_ENDPOINT_INDEX_KEY)).thenReturn("resources");
        when(environment.readEnv(ELASTICSEARCH_ENDPOINT_API_SCHEME_KEY)).thenReturn("https");
    }

    /**
     * Set up test environment.
     **/
    @BeforeEach
    public void init() {
        environment = mock(Environment.class);
        initEnvironment();
        elasticSearchRestClient = new ElasticSearchHighLevelRestClient(environment);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void defaultConstructorWithEnvironmentIsNullShouldFail() {
        assertThrows(NullPointerException.class, () -> new ElasticSearchHighLevelRestClient(null));
    }

    @Test
    public void constructorWithEnvironmentDefinedShouldCreateInstance() {
        ElasticSearchHighLevelRestClient elasticSearchRestClient = new ElasticSearchHighLevelRestClient(environment);
        assertNotNull(elasticSearchRestClient);
    }


    @Test
    public void searchSingleTermReturnsResponse() throws ApiGatewayException, IOException {

        RestHighLevelClient restHighLevelClient = mock(RestHighLevelClient.class);
        SearchResponse searchResponse = mock(SearchResponse.class);
        when(searchResponse.toString()).thenReturn(SAMPLE_JSON_RESPONSE);
        when(restHighLevelClient.search(any(), any())).thenReturn(searchResponse);
        ElasticSearchHighLevelRestClient elasticSearchRestClient =
                new ElasticSearchHighLevelRestClient(environment, restHighLevelClient);
        QueryContentsResponse queryContentsResponse =
                elasticSearchRestClient.searchSingleTerm(SAMPLE_TERM,
                        SAMPLE_NUMBER_OF_RESULTS,
                        SAMPLE_FROM,
                        SAMPLE_ORDERBY,
                        SortOrder.DESC);
        assertNotNull(queryContentsResponse);
    }

    @Test
    public void searchSingleTermReturnsResponseWithStatsFromElastic() throws ApiGatewayException, IOException {

        RestHighLevelClient restHighLevelClient = mock(RestHighLevelClient.class);
        SearchResponse searchResponse = mock(SearchResponse.class);
        String elasticSearchResponseJson = getElasticSEarchResponseAsString();
        when(searchResponse.toString()).thenReturn(elasticSearchResponseJson);
        when(restHighLevelClient.search(any(), any())).thenReturn(searchResponse);
        ElasticSearchHighLevelRestClient elasticSearchRestClient =
                new ElasticSearchHighLevelRestClient(environment, restHighLevelClient);
        QueryContentsResponse queryContentsResponse =
                elasticSearchRestClient.searchSingleTerm(SAMPLE_TERM,
                        MAX_RESULTS,
                        SAMPLE_FROM,
                        SAMPLE_ORDERBY,
                        SortOrder.DESC);
        assertNotNull(queryContentsResponse);
        assertEquals(queryContentsResponse.getTotal(), ELASTIC_ACTUAL_SAMPLE_NUMBER_OF_RESULTS);
    }

    private String getElasticSEarchResponseAsString() {
        return IoUtils.streamToString(IoUtils.inputStreamFromResources(ELASTIC_SAMPLE_RESPONSE_FILE));
    }


    @Test
    public void searchSingleTermReturnsErrorResponseWhenExceptionInDoSearch() throws ApiGatewayException, IOException {

        RestHighLevelClient restHighLevelClient = mock(RestHighLevelClient.class);
        when(restHighLevelClient.search(any(), any())).thenThrow(new IOException());

        ElasticSearchHighLevelRestClient elasticSearchRestClient =
                new ElasticSearchHighLevelRestClient(environment, restHighLevelClient);

        assertThrows(SearchException.class, () -> elasticSearchRestClient.searchSingleTerm(SAMPLE_TERM,
                        SAMPLE_NUMBER_OF_RESULTS,
                        SAMPLE_FROM,
                        SAMPLE_ORDERBY,
                        SortOrder.DESC));

    }



    @Test
    public void addDocumentToIndexThrowsException() throws IOException {

        IndexDocument indexDocument = mock(IndexDocument.class);
        doThrow(RuntimeException.class).when(indexDocument).toJsonString();
        RestHighLevelClient restHighLevelClient = mock(RestHighLevelClient.class);
        when(restHighLevelClient.update(any(), any())).thenThrow(new RuntimeException());
        ElasticSearchHighLevelRestClient elasticSearchRestClient =
                new ElasticSearchHighLevelRestClient(environment, restHighLevelClient);

        assertThrows(SearchException.class, () -> elasticSearchRestClient.addDocumentToIndex(indexDocument));
    }

    @Test
    public void removeDocumentThrowsException() throws IOException {

        IndexDocument indexDocument = mock(IndexDocument.class);
        doThrow(RuntimeException.class).when(indexDocument).toJsonString();
        RestHighLevelClient restHighLevelClient = mock(RestHighLevelClient.class);
        when(restHighLevelClient.update(any(), any())).thenThrow(new RuntimeException());
        ElasticSearchHighLevelRestClient elasticSearchRestClient =
                new ElasticSearchHighLevelRestClient(environment, restHighLevelClient);

        assertThrows(SearchException.class, () -> elasticSearchRestClient.removeDocumentFromIndex(""));
    }

    @Test
    public void removeDocumentReturnsDocumentNotFoundWhenNoDocumentMatchesIdentifier() throws IOException,
            SearchException {

        RestHighLevelClient restHighLevelClient = mock(RestHighLevelClient.class);
        DeleteResponse nothingFoundResponse = mock(DeleteResponse.class);
        when(nothingFoundResponse.getResult()).thenReturn(DocWriteResponse.Result.NOT_FOUND);
        when(restHighLevelClient.delete(any(), any())).thenReturn(nothingFoundResponse);
        ElasticSearchHighLevelRestClient elasticSearchRestClient =
                new ElasticSearchHighLevelRestClient(environment, restHighLevelClient);
        elasticSearchRestClient.removeDocumentFromIndex("1234");
    }

    @Test
    public void addDocumentToIndex() throws IOException, SearchException {

        UpdateResponse updateResponse = mock(UpdateResponse.class);
        IndexDocument mockDocument = mock(IndexDocument.class);
        when(mockDocument.toJsonString()).thenReturn("{}");
        when(mockDocument.getId()).thenReturn(UUID.randomUUID());
        RestHighLevelClient restHighLevelClient = mock(RestHighLevelClient.class);
        when(restHighLevelClient.update(any(), any())).thenReturn(updateResponse);

        ElasticSearchHighLevelRestClient elasticSearchRestClient =
                new ElasticSearchHighLevelRestClient(environment, restHighLevelClient);

        elasticSearchRestClient.addDocumentToIndex(mockDocument);
    }
}
