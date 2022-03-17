package no.unit.bibs.contents;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.bibs.contents.exception.CommunicationException;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static no.unit.bibs.contents.DynamoDBClient.DOCUMENT_WITH_ID_WAS_NOT_FOUND;
import static no.unit.bibs.contents.DynamoDBClient.PRIMARYKEY_ISBN;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DynamoDBClientTest {

    public static final String SAMPLE_TERM = "SampleSearchTerm";
    public static final String CREATE_CONTENTS_EVENT = "createContentsEvent.json";
    public static final String GET_CONTENTS_JSON = "get_contents.json";

    DynamoDBClient dynamoDBClient;
    private DynamoDbClient client;


    /**
     * Set up test environment.
     **/
    @BeforeEach
    public void init() {
        client = mock(DynamoDbClient.class);
        dynamoDBClient = new DynamoDBClient(client);
    }

    @Test
    public void constructorWithEnvironmentDefinedShouldCreateInstance() {
        DynamoDBClient dynamoDBClient = new DynamoDBClient(client);
        assertNotNull(dynamoDBClient);
    }


    @Test
    void handlerReturnsNotFoundExceptionWhenShittyResponseFromDynamoDB()  {
        DynamoDbClient client = mock(DynamoDbClient.class);
        DynamoDBClient dbClient = new DynamoDBClient(client);
        when(client.getItem(any(GetItemRequest.class))).thenReturn(null);
        Exception exception = assertThrows(NotFoundException.class, () -> dbClient.getContents(SAMPLE_TERM));

        String expectedMessage = String.format(DOCUMENT_WITH_ID_WAS_NOT_FOUND, SAMPLE_TERM);
        String actualMessage = exception.getMessage();

        assertEquals(actualMessage, (expectedMessage));

        GetItemResponse getItemResponse = mock(GetItemResponse.class);
        when(client.getItem(any(GetItemRequest.class))).thenReturn(getItemResponse);
        when(getItemResponse.item()).thenReturn(new HashMap<>());
        exception = assertThrows(NotFoundException.class, () -> dbClient.getContents(SAMPLE_TERM));

        actualMessage = exception.getMessage();

        assertEquals(actualMessage, (expectedMessage));
    }


    @Test
    public void searchSingleTermReturnsResponse() throws ApiGatewayException {
        DynamoDbClient client = mock(DynamoDbClient.class);
        String contents = IoUtils.stringFromResources(Path.of(GET_CONTENTS_JSON));
        Item item = new Item();
        item.withJSON("contents", contents);
        GetItemResponse getItemResponse = mock(GetItemResponse.class);
        Map<String, AttributeValue> returnedItem = new HashMap<>();
        returnedItem.put(PRIMARYKEY_ISBN, AttributeValue.builder().s(SAMPLE_TERM).build());
        DynamoDBClient dynamoDBClient = new DynamoDBClient(client);
        when(client.getItem(any(GetItemRequest.class))).thenReturn(getItemResponse);
        when(getItemResponse.item()).thenReturn(returnedItem);
        String getContentsResponse = dynamoDBClient.getContents(SAMPLE_TERM);
        assertNotNull(getContentsResponse);
    }

    @Test
    public void addDocumentToIndexThrowsException() {
        ContentsDocument document = mock(ContentsDocument.class);
        when(document.getIsbn()).thenReturn(SAMPLE_TERM);
        when(document.getSource()).thenReturn(SAMPLE_TERM);
        doThrow(IllegalArgumentException.class).when(client).putItem(any(PutItemRequest.class));
        DynamoDBClient dynamoDBClient = new DynamoDBClient(client);
        assertThrows(CommunicationException.class, () -> dynamoDBClient.createContents(document));
    }


    @Test
    public void searchSingleTermReturnsErrorResponseWhenExceptionInDoSearch() {
        DynamoDbClient client = mock(DynamoDbClient.class);
        DynamoDBClient dynamoDBClient = new DynamoDBClient(client);
        assertThrows(NotFoundException.class, () -> dynamoDBClient.getContents(SAMPLE_TERM));
    }
    
    @Test
    public void addDocumentTest() throws IOException, CommunicationException {
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        ContentsDocument document = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        DynamoDbClient client = mock(DynamoDbClient.class);
        DynamoDBClient dynamoDBClient = new DynamoDBClient(client);
        PutItemResponse putItemReponse = mock(PutItemResponse.class);
        when(client.putItem(any(PutItemRequest.class))).thenReturn(putItemReponse);
        when(putItemReponse.hasAttributes()).thenReturn(true);
        dynamoDBClient.createContents(document);
    }

    @Test
    public void testUpdateContents() throws CommunicationException, JsonProcessingException {
        DynamoDbClient client = mock(DynamoDbClient.class);
        UpdateItemResponse updateItemResponse = mock(UpdateItemResponse.class);
        when(client.updateItem(any(UpdateItemRequest.class))).thenReturn(updateItemResponse);
        Map<String, AttributeValue> returnedItem = new HashMap<>();
        returnedItem.put(PRIMARYKEY_ISBN, AttributeValue.builder().s(SAMPLE_TERM).build());
        when(updateItemResponse.attributes()).thenReturn(returnedItem);
        String contents = IoUtils.stringFromResources(Path.of(GET_CONTENTS_JSON));
        ContentsDocument document = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        DynamoDBClient dynamoDBClient = new DynamoDBClient(client);
        String updateContents = dynamoDBClient.updateContents(document);
        assertEquals(dynamoDBClient.parseAttributeValueMap(returnedItem), updateContents);
    }

    @Test
    public void testUpdateContentsThrowsException() throws JsonProcessingException {
        DynamoDbClient client = mock(DynamoDbClient.class);
        UpdateItemResponse updateItemResponse = mock(UpdateItemResponse.class);
        DynamoDBClient dynamoDBClient = new DynamoDBClient(client);
        when(client.updateItem(any(UpdateItemRequest.class))).thenReturn(updateItemResponse);
        String contents = IoUtils.stringFromResources(Path.of(GET_CONTENTS_JSON));
        ContentsDocument document = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        Exception exception = assertThrows(CommunicationException.class, () -> dynamoDBClient.updateContents(document));

        String expectedMessage = "Update error:";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

}
