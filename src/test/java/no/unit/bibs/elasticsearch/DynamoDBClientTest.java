package no.unit.bibs.elasticsearch;

import com.amazonaws.services.dynamodbv2.document.Table;
import no.unit.bibs.elasticsearch.exception.SearchException;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.utils.IoUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DynamoDBClientTest {

    public static final String SAMPLE_TERM = "SampleSearchTerm";
    private static final String SAMPLE_JSON_RESPONSE = "{}";
    private static final String ELASTIC_SAMPLE_RESPONSE_FILE = "sample_elasticsearch_response.json";
    private static final int ELASTIC_ACTUAL_SAMPLE_NUMBER_OF_RESULTS = 2;

    DynamoDBClient dynamoDBClient;
    private Table dynamoTable;


    /**
     * Set up test environment.
     **/
    @BeforeEach
    public void init() {
        dynamoTable = mock(Table.class);
        dynamoDBClient = new DynamoDBClient(dynamoTable);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void defaultConstructorWithEnvironmentIsNullShouldFail() {
        assertThrows(NullPointerException.class, () -> new DynamoDBClient(dynamoTable));
    }

    @Test
    public void constructorWithEnvironmentDefinedShouldCreateInstance() {
        DynamoDBClient dynamoDBClient = new DynamoDBClient(dynamoTable);
        assertNotNull(dynamoDBClient);
    }


    @Test
    public void searchSingleTermReturnsResponse() throws ApiGatewayException, IOException {
        SearchResponse searchResponse = mock(SearchResponse.class);
        when(searchResponse.toString()).thenReturn(SAMPLE_JSON_RESPONSE);
        DynamoDBClient dynamoDBClient = new DynamoDBClient(dynamoTable);
        QueryContentsResponse queryContentsResponse = dynamoDBClient.get(SAMPLE_TERM);
        assertNotNull(queryContentsResponse);
    }

    @Test
    public void addDocumentToIndexThrowsException() throws IOException {
        ContentsDocument contentsDocument = mock(ContentsDocument.class);
        doThrow(RuntimeException.class).when(contentsDocument).toJsonString();
        RestHighLevelClient restHighLevelClient = mock(RestHighLevelClient.class);
        when(restHighLevelClient.update(any(), any())).thenThrow(new RuntimeException());
        DynamoDBClient dynamoDBClient = new DynamoDBClient(dynamoTable);
        assertThrows(SearchException.class, () -> dynamoDBClient.addDocument(contentsDocument));
    }

    @Test
    public void searchSingleTermReturnsResponseWithStatsFromElastic() throws ApiGatewayException, IOException {
        RestHighLevelClient restHighLevelClient = mock(RestHighLevelClient.class);
        SearchResponse searchResponse = mock(SearchResponse.class);
        String elasticSearchResponseJson = getElasticSEarchResponseAsString();
        when(searchResponse.toString()).thenReturn(elasticSearchResponseJson);
        when(restHighLevelClient.search(any(), any())).thenReturn(searchResponse);
        DynamoDBClient dynamoDBClient = new DynamoDBClient(dynamoTable);
        QueryContentsResponse queryContentsResponse = dynamoDBClient.get(SAMPLE_TERM);
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
        DynamoDBClient dynamoDBClient = new DynamoDBClient(dynamoTable);
        assertThrows(SearchException.class, () -> dynamoDBClient.get(SAMPLE_TERM));
    }
    
    @Test
    public void addDocumentToIndex() throws IOException, SearchException {
        UpdateResponse updateResponse = mock(UpdateResponse.class);
        ContentsDocument mockDocument = mock(ContentsDocument.class);
        when(mockDocument.toJsonString()).thenReturn("{}");
        when(mockDocument.getIsbn()).thenReturn(SAMPLE_TERM);
        RestHighLevelClient restHighLevelClient = mock(RestHighLevelClient.class);
        when(restHighLevelClient.update(any(), any())).thenReturn(updateResponse);
        DynamoDBClient dynamoDBClient = new DynamoDBClient(dynamoTable);
        dynamoDBClient.addDocument(mockDocument);
    }
    
}
