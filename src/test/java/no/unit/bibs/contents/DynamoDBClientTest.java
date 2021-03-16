package no.unit.bibs.contents;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import no.unit.bibs.contents.exception.CommunicationException;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.exceptions.commonexceptions.NotFoundException;
import nva.commons.utils.IoUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static nva.commons.utils.JsonUtils.objectMapper;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DynamoDBClientTest {

    public static final String SAMPLE_TERM = "SampleSearchTerm";
    private static final String SAMPLE_JSON_RESPONSE = "{}";
    private static final String ELASTIC_SAMPLE_RESPONSE_FILE = "sample_elasticsearch_response.json";
    public static final String CREATE_CONTENTS_EVENT = "createContentsEvent.json";
    public static final String GET_CONTENTS_JSON = "get_contents.json";

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
        String contents = IoUtils.stringFromResources(Path.of(GET_CONTENTS_JSON));
        Item item = new Item();
        item.withJSON("contents", contents);
        when(dynamoTable.getItem(anyString(), anyString())).thenReturn(item);
        String getContentsResponse = dynamoDBClient.getContents(SAMPLE_TERM);
        assertNotNull(getContentsResponse);
    }

    @Test
    public void addDocumentToIndexThrowsException() throws IOException {
        ContentsDocument contentsDocument = mock(ContentsDocument.class);
        doThrow(RuntimeException.class).when(contentsDocument).toJsonString();
        RestHighLevelClient restHighLevelClient = mock(RestHighLevelClient.class);
        when(restHighLevelClient.update(any(), any())).thenThrow(new RuntimeException());
        DynamoDBClient dynamoDBClient = new DynamoDBClient(dynamoTable);
        assertThrows(CommunicationException.class, () -> dynamoDBClient.addContents(contentsDocument));
    }


    @Test
    public void searchSingleTermReturnsErrorResponseWhenExceptionInDoSearch() throws IOException {
        RestHighLevelClient restHighLevelClient = mock(RestHighLevelClient.class);
        when(restHighLevelClient.search(any(), any())).thenThrow(new IOException());
        DynamoDBClient dynamoDBClient = new DynamoDBClient(dynamoTable);
        assertThrows(NotFoundException.class, () -> dynamoDBClient.getContents(SAMPLE_TERM));
    }
    
    @Test
    public void addDocumentTest() throws IOException {
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        ContentsDocument contentsDocument = objectMapper.readValue(contents, ContentsDocument.class);
        DynamoDBClient dynamoDBClient = new DynamoDBClient(dynamoTable);
        try {
            dynamoDBClient.addContents(contentsDocument);
        } catch (no.unit.bibs.contents.exception.CommunicationException e) {
            e.printStackTrace();
        }
    }
    
}
