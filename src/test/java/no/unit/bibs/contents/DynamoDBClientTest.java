package no.unit.bibs.contents;

import static no.unit.bibs.contents.DynamoDBClient.DOCUMENT_WITH_ID_WAS_NOT_FOUND;
import static nva.commons.core.JsonUtils.dtoObjectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import no.unit.bibs.contents.exception.CommunicationException;
import no.unit.nva.testutils.IoUtils;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DynamoDBClientTest {

    public static final String SAMPLE_TERM = "SampleSearchTerm";
    public static final String CREATE_CONTENTS_EVENT = "createContentsEvent.json";
    public static final String GET_CONTENTS_JSON = "get_contents.json";
    public static final String KEY = "key";
    private static final String EMPTY_JSON = "{}";

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
        ContentsDocument document = mock(ContentsDocument.class);
        when(document.getIsbn()).thenReturn(SAMPLE_TERM);
        when(document.getSource()).thenReturn(SAMPLE_TERM);
        doThrow(IllegalArgumentException.class).when(dynamoTable).putItem(any(PutItemSpec.class));
        DynamoDBClient dynamoDBClient = new DynamoDBClient(dynamoTable);
        assertThrows(CommunicationException.class, () -> dynamoDBClient.createContents(document));
    }


    @Test
    public void searchSingleTermReturnsErrorResponseWhenExceptionInDoSearch() {
        DynamoDBClient dynamoDBClient = new DynamoDBClient(dynamoTable);
        assertThrows(NotFoundException.class, () -> dynamoDBClient.getContents(SAMPLE_TERM));
    }
    
    @Test
    public void addDocumentTest() throws IOException, CommunicationException {
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        ContentsDocument document = dtoObjectMapper.readValue(contents, ContentsDocument.class);
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
        ContentsDocument document = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        String updateContents = client.updateContents(document);
        assertEquals(EMPTY_JSON, updateContents);
    }

    @Test
    public void testUpdateContentsThrowsException() throws JsonProcessingException {
        Table table = mock(Table.class);
        DynamoDBClient client = new DynamoDBClient(table);
        UpdateItemOutcome outcome = mock(UpdateItemOutcome.class);
        when(table.updateItem(any(UpdateItemSpec.class))).thenReturn(outcome);
        String contents = IoUtils.stringFromResources(Path.of(GET_CONTENTS_JSON));
        ContentsDocument document = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        when(table.getItem(ContentsDocument.ISBN, SAMPLE_TERM)).thenReturn(new Item());
        when(outcome.getItem()).thenThrow(IllegalArgumentException.class);
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
        dynamoDBClient.conditionalAdd(map, SAMPLE_TERM, KEY, false);
        assertEquals(SAMPLE_TERM, map.get(KEY));

        String input = "&Oslash;konomi er g&oslash;y.";
        dynamoDBClient.conditionalAdd(map, input, ContentsDocument.DESCRIPTION_SHORT, false);
        assertEquals(input, map.get(ContentsDocument.DESCRIPTION_SHORT));

        String expected = new String("Økonomi er gøy.".getBytes(), StandardCharsets.UTF_8);
        assertNotEquals(expected, input);
        dynamoDBClient.conditionalAdd(map, input, ContentsDocument.DESCRIPTION_SHORT, true);
        assertEquals(expected, map.get(ContentsDocument.DESCRIPTION_SHORT));

        String inputNonEscaped = new String("Økonomi er gøy.".getBytes(), StandardCharsets.UTF_8);
        dynamoDBClient.conditionalAdd(map, inputNonEscaped, ContentsDocument.DESCRIPTION_SHORT, true);
        assertEquals(expected, map.get(ContentsDocument.DESCRIPTION_SHORT));

        dynamoDBClient.conditionalAdd(map, inputNonEscaped, ContentsDocument.DESCRIPTION_SHORT, false);
        assertEquals(expected, map.get(ContentsDocument.DESCRIPTION_SHORT));
        
        
        Item item = new Item();
        dynamoDBClient.conditionalAdd(item, SAMPLE_TERM, KEY, false);
        assertEquals(SAMPLE_TERM, item.get(KEY));

        dynamoDBClient.conditionalAdd(item, input, ContentsDocument.DESCRIPTION_SHORT, false);
        assertEquals(input, item.get(ContentsDocument.DESCRIPTION_SHORT));

        dynamoDBClient.conditionalAdd(item, input, ContentsDocument.DESCRIPTION_SHORT, true);
        assertEquals(expected, item.get(ContentsDocument.DESCRIPTION_SHORT));

        dynamoDBClient.conditionalAdd(item, inputNonEscaped, ContentsDocument.DESCRIPTION_SHORT, true);
        assertEquals(expected, item.get(ContentsDocument.DESCRIPTION_SHORT));

        dynamoDBClient.conditionalAdd(item, inputNonEscaped, ContentsDocument.DESCRIPTION_SHORT, false);
        assertEquals(expected, item.get(ContentsDocument.DESCRIPTION_SHORT));
    }

}
