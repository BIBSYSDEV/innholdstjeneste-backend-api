package no.unit.bibs.elasticsearch;

import no.unit.bibs.elasticsearch.exception.SearchException;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.utils.Environment;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ElasticSearchSigningHighLevelRestClientTest {

    private static final String elasticSearchEndpoint = "http://localhost";
    public static final String SAMPLE_TERM = "SampleSearchTerm";
    private static final int SAMPLE_NUMBER_OF_RESULTS = 7;
    private static final String SAMPLE_JSON_RESPONSE = "{}";
    private static final int SAMPLE_FROM = 0;
    private static final String SAMPLE_ORDERBY = "orderByField";

    DynamoDBClient elasticSearchRestClient;
    private Environment environment;

    private void initEnvironment() {
    }

    /**
     * Set up test environment.
     **/
    @BeforeEach
    public void init() {
        environment = mock(Environment.class);
        initEnvironment();
        elasticSearchRestClient = new DynamoDBClient(environment);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void defaultConstructorWithEnvironmentIsNullShouldFail() {
        assertThrows(NullPointerException.class, () -> new DynamoDBClient(null));
    }

    @Test
    public void constructorWithEnvironmentDefinedShouldCreateInstance() {
        DynamoDBClient elasticSearchRestClient = new DynamoDBClient(environment);
        assertNotNull(elasticSearchRestClient);
    }


    @Test
    public void searchSingleTermReturnsResponse() throws ApiGatewayException, IOException {

        RestHighLevelClient restHighLevelClient = mock(RestHighLevelClient.class);
        SearchResponse searchResponse = mock(SearchResponse.class);
        when(searchResponse.toString()).thenReturn(SAMPLE_JSON_RESPONSE);
        when(restHighLevelClient.search(any(), any())).thenReturn(searchResponse);
        DynamoDBClient elasticSearchRestClient =
                new DynamoDBClient(environment, restHighLevelClient);
        QueryContentsResponse queryContentsResponse =
                elasticSearchRestClient.searchSingleTerm(SAMPLE_TERM,
                        SAMPLE_NUMBER_OF_RESULTS,
                        SAMPLE_FROM
                );
        assertNotNull(queryContentsResponse);
    }

    @Test
    public void addDocumentToIndexThrowsException() throws IOException {

        ContentsDocument contentsDocument = mock(ContentsDocument.class);
        doThrow(RuntimeException.class).when(contentsDocument).toJsonString();
        RestHighLevelClient restHighLevelClient = mock(RestHighLevelClient.class);
        when(restHighLevelClient.update(any(), any())).thenThrow(new RuntimeException());
        DynamoDBClient elasticSearchRestClient =
                new DynamoDBClient(environment, restHighLevelClient);

        assertThrows(SearchException.class, () -> elasticSearchRestClient.addDocumentToIndex(contentsDocument));
    }

    @Test
    public void removeDocumentThrowsException() throws IOException {

        ContentsDocument contentsDocument = mock(ContentsDocument.class);
        doThrow(RuntimeException.class).when(contentsDocument).toJsonString();
        RestHighLevelClient restHighLevelClient = mock(RestHighLevelClient.class);
        when(restHighLevelClient.update(any(), any())).thenThrow(new RuntimeException());
        DynamoDBClient elasticSearchRestClient =
                new DynamoDBClient(environment, restHighLevelClient);

        assertThrows(SearchException.class, () -> elasticSearchRestClient.removeDocumentFromIndex(""));
    }

    @Test
    public void removeDocumentReturnsDocumentNotFoundWhenNoDocumentMatchesIdentifier() throws IOException,
            SearchException {

        RestHighLevelClient restHighLevelClient = mock(RestHighLevelClient.class);
        DeleteResponse nothingFoundResponse = mock(DeleteResponse.class);
        when(nothingFoundResponse.getResult()).thenReturn(DocWriteResponse.Result.NOT_FOUND);
        when(restHighLevelClient.delete(any(), any())).thenReturn(nothingFoundResponse);
        DynamoDBClient elasticSearchRestClient =
                new DynamoDBClient(environment, restHighLevelClient);
        elasticSearchRestClient.removeDocumentFromIndex("1234");
    }

    @Test
    public void addDocumentToIndex() throws IOException, SearchException {

        UpdateResponse updateResponse = mock(UpdateResponse.class);
        ContentsDocument mockDocument = mock(ContentsDocument.class);
        when(mockDocument.toJsonString()).thenReturn("{}");
        when(mockDocument.getId()).thenReturn(UUID.randomUUID());
        RestHighLevelClient restHighLevelClient = mock(RestHighLevelClient.class);
        when(restHighLevelClient.update(any(), any())).thenReturn(updateResponse);

        DynamoDBClient elasticSearchRestClient =
                new DynamoDBClient(environment, restHighLevelClient);

        elasticSearchRestClient.addDocumentToIndex(mockDocument);
    }
}
