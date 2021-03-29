package no.unit.bibs.contents;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.bibs.contents.exception.CommunicationException;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.exceptions.commonexceptions.NotFoundException;
import nva.commons.utils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static no.unit.bibs.contents.DynamoDBClient.DOCUMENT_WITH_ID_WAS_NOT_FOUND;
import static no.unit.bibs.contents.GatewayResponse.EMPTY_JSON;
import static nva.commons.utils.JsonUtils.objectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DynamoDBClientTest {

    public static final String SAMPLE_TERM = "SampleSearchTerm";
    public static final String CREATE_CONTENTS_EVENT = "createContentsEvent.json";
    public static final String GET_CONTENTS_JSON = "get_contents.json";
    public static final String KEY = "key";

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
    void handlerReturnsNotFoundExceptionWhenShittyResponseFromDynamoDB()  {
        Table table = mock(Table.class);
        DynamoDBClient dbClient = new DynamoDBClient(table);
        when(table.getItem(ContentsDocument.ISBN, SAMPLE_TERM)).thenReturn(null);
        Exception exception = assertThrows(NotFoundException.class, () -> {
            dbClient.getContents(SAMPLE_TERM);
        });

        String expectedMessage = String.format(DOCUMENT_WITH_ID_WAS_NOT_FOUND, SAMPLE_TERM);
        String actualMessage = exception.getMessage();

        assertEquals(actualMessage, (expectedMessage));

        when(table.getItem(ContentsDocument.ISBN, SAMPLE_TERM)).thenReturn(new Item());
        exception = assertThrows(NotFoundException.class, () -> {
            dbClient.getContents(SAMPLE_TERM);
        });

        actualMessage = exception.getMessage();

        assertEquals(actualMessage, (expectedMessage));
    }


    @Test
    public void searchSingleTermReturnsResponse() throws ApiGatewayException, IOException {
        DynamoDBClient dynamoDBClient = new DynamoDBClient(dynamoTable);
        String contents = IoUtils.stringFromResources(Path.of(GET_CONTENTS_JSON));
        Item item = new Item();
        item.withJSON("contents", contents);
        when(dynamoTable.getItem(anyString(), anyString())).thenReturn(item);
        String getContentsResponse = dynamoDBClient.getContents(SAMPLE_TERM);
        assertNotNull(getContentsResponse);
    }

    @Test
    public void addDocumentToIndexThrowsException() {
        ContentsDocument contentsDocument = mock(ContentsDocument.class);
        when(contentsDocument.getIsbn()).thenReturn(SAMPLE_TERM);
        doThrow(RuntimeException.class).when(contentsDocument).toJsonString();
        DynamoDBClient dynamoDBClient = new DynamoDBClient(dynamoTable);
        assertThrows(CommunicationException.class, () -> dynamoDBClient.createContents(contentsDocument));
    }


    @Test
    public void searchSingleTermReturnsErrorResponseWhenExceptionInDoSearch() {
        DynamoDBClient dynamoDBClient = new DynamoDBClient(dynamoTable);
        assertThrows(NotFoundException.class, () -> dynamoDBClient.getContents(SAMPLE_TERM));
    }
    
    @Test
    public void addDocumentTest() throws IOException, CommunicationException, NotFoundException {
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        ContentsDocument document = objectMapper.readValue(contents, ContentsDocument.class);
        DynamoDBClient dynamoDBClient = new DynamoDBClient(dynamoTable);
        PutItemOutcome putItemOutcome = mock(PutItemOutcome.class);
        Item item = new Item()
            .withString("isbn", document.getIsbn())
            .withString("source", document.getSource())
            .withString("created", Instant.now().toString());
        when(putItemOutcome.getItem()).thenReturn(item);
        when(dynamoTable.putItem(any(PutItemSpec.class))).thenReturn(putItemOutcome);
        dynamoDBClient.createContents(document);
    }

    @Test
    public void testUpdateContents() throws CommunicationException, JsonProcessingException {
        Table table = mock(Table.class);
        DynamoDBClient client = new DynamoDBClient(table);
        UpdateItemOutcome outcome = mock(UpdateItemOutcome.class);
        when(table.updateItem(any(UpdateItemSpec.class))).thenReturn(outcome);
        Item item = mock(Item.class);
        when(outcome.getItem()).thenReturn(item);
        when(item.toJSON()).thenReturn(EMPTY_JSON);
        String contents = IoUtils.stringFromResources(Path.of(GET_CONTENTS_JSON));
        ContentsDocument document = objectMapper.readValue(contents, ContentsDocument.class);
        String updateContents = client.updateContents(document);
        assertEquals(EMPTY_JSON, updateContents);
    }

    @Test
    public void testUpdateContentsThrowsException() throws CommunicationException, JsonProcessingException {
        Table table = mock(Table.class);
        UpdateItemOutcome outcome = mock(UpdateItemOutcome.class);
        DynamoDBClient client = new DynamoDBClient(table);
        when(table.updateItem(any(UpdateItemSpec.class))).thenReturn(null);
        String contents = IoUtils.stringFromResources(Path.of(GET_CONTENTS_JSON));
        ContentsDocument document = objectMapper.readValue(contents, ContentsDocument.class);
        when(table.getItem(ContentsDocument.ISBN, SAMPLE_TERM)).thenReturn(new Item());
        Exception exception = assertThrows(CommunicationException.class, () -> {
            client.updateContents(document);
        });

        String expectedMessage = "Update error:";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testConditionalAdd() {
        Map<String, String> map = new HashMap<>();
        dynamoDBClient.conditionalAdd(map, SAMPLE_TERM, KEY);
        assertEquals(SAMPLE_TERM, map.get(KEY));
    }

}
