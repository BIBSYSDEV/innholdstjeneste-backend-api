package no.unit.bibs.contents;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.Instant;
import no.unit.bibs.contents.exception.CommunicationException;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
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

import static no.unit.bibs.contents.DBClient.DOCUMENT_WITH_ID_WAS_NOT_FOUND;
import static no.unit.bibs.contents.DBClient.PRIMARYKEY_ISBN;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DBClientTest {

    public static final String SAMPLE_TERM = "SampleSearchTerm";
    public static final String CREATE_CONTENTS_EVENT = "createContentsEvent.json";
    public static final String GET_CONTENTS_JSON = "get_contents.json";

    private DBClient dbClient;
    private DynamoDbClient client;


    /**
     * Set up test environment.
     **/
    @BeforeEach
    public void init() {
        client = mock(DynamoDbClient.class);
        dbClient = new DBClient(client);
    }

    @Test
    void handlerReturnsNotFoundExceptionWhenShittyResponseFromDynamoDB()  {
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
        GetItemResponse getItemResponse = mock(GetItemResponse.class);
        Map<String, AttributeValue> returnedItem = new HashMap<>();
        returnedItem.put(PRIMARYKEY_ISBN, AttributeValue.builder().s(SAMPLE_TERM).build());
        when(client.getItem(any(GetItemRequest.class))).thenReturn(getItemResponse);
        when(getItemResponse.item()).thenReturn(returnedItem);
        String getContentsResponse = dbClient.getContents(SAMPLE_TERM);

        assertNotNull(getContentsResponse);
        assertThat(getContentsResponse, containsString(SAMPLE_TERM));
    }

    @Test
    public void addDocumentToIndexThrowsException() {
        ContentsDocument document = mock(ContentsDocument.class);
        when(document.getIsbn()).thenReturn(SAMPLE_TERM);
        when(document.getSource()).thenReturn(SAMPLE_TERM);
        doThrow(IllegalArgumentException.class).when(client).putItem(any(PutItemRequest.class));
        assertThrows(CommunicationException.class, () -> dbClient.createContents(document));
    }


    @Test
    public void searchSingleTermReturnsErrorResponseWhenExceptionInDoSearch() {
        assertThrows(NotFoundException.class, () -> dbClient.getContents(SAMPLE_TERM));
    }

    @Test
    public void addDocumentTest() throws IOException, CommunicationException {
        var contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        final var document = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        var putItemResponse = mock(PutItemResponse.class);

        when(client.putItem(any(PutItemRequest.class)))
            .thenReturn(putItemResponse);
        when(putItemResponse.hasAttributes())
            .thenReturn(true);

        dbClient.createContents(document);

        verify(client, times(1)).putItem(any(PutItemRequest.class));
    }

    @Test
    public void testUpdateContents() throws CommunicationException, JsonProcessingException {
        UpdateItemResponse updateItemResponse = mock(UpdateItemResponse.class);
        when(client.updateItem(any(UpdateItemRequest.class))).thenReturn(updateItemResponse);
        Map<String, AttributeValue> returnedItem = new HashMap<>();
        returnedItem.put(PRIMARYKEY_ISBN, AttributeValue.builder().s(SAMPLE_TERM).build());
        when(updateItemResponse.attributes()).thenReturn(returnedItem);
        String contents = IoUtils.stringFromResources(Path.of(GET_CONTENTS_JSON));
        ContentsDocument document = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        dbClient.updateContents(document);

        verify(client, times(1)).updateItem(any(UpdateItemRequest.class));
    }

    @Test
    void shouldUseExistingDateOfCreationWhenCreatingNewContent() throws Exception {
        var contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        var document = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        document = spy(document);
        var created = "2025-02-13T10:00:00Z";
        var testInstant = Instant.parse(created);
        doReturn(testInstant).when(document).getCreated();

        assertThat(document.getCreated(), equalTo(testInstant));

        var putItemResponse = mock(PutItemResponse.class);

        when(client.putItem(any(PutItemRequest.class)))
            .thenReturn(putItemResponse);
        when(putItemResponse.hasAttributes())
            .thenReturn(true);

        dbClient.createContents(document);

        var captor = ArgumentCaptor.forClass(PutItemRequest.class);

        verify(client, times(1)).putItem(captor.capture());

        var capturedCreated = captor.getValue().item().get("created");

        assertThat(capturedCreated.s(), containsString(created));
    }

    @Test
    void shouldThrowNotFoundOnDynamoDbErrorWhenGettingContents() {
        doThrow(DynamoDbException.class).when(client).getItem(any(GetItemRequest.class));

        var exception = assertThrows(NotFoundException.class, () -> dbClient.getContents(SAMPLE_TERM));
        assertThat(exception.getMessage(), containsString(String.format(DOCUMENT_WITH_ID_WAS_NOT_FOUND, SAMPLE_TERM)));
    }

    @Test
    void shouldThrowCommunicationErrorOnDynamoDbErrorWhenUpdatingContents() throws Exception {
        doThrow(DynamoDbException.class).when(client).updateItem(any(UpdateItemRequest.class));

        var contents = IoUtils.stringFromResources(Path.of(GET_CONTENTS_JSON));
        var document = dtoObjectMapper.readValue(contents, ContentsDocument.class);

        var exception = assertThrows(CommunicationException.class, () -> dbClient.updateContents(document));
        assertThat(exception.getMessage(), containsString("Update error"));
    }

    @Test
    void shouldThrowNotFoundExceptionOnEmptyResponseFromDynamoDb() {
        var itemResponse = mock(GetItemResponse.class);
        doReturn(null).when(itemResponse).item();
        when(client.getItem(any(GetItemRequest.class))).thenReturn(itemResponse);
        var exception = assertThrows(NotFoundException.class, () -> dbClient.getContents(SAMPLE_TERM));

        assertThat(exception.getMessage(), containsString(String.format(DOCUMENT_WITH_ID_WAS_NOT_FOUND, SAMPLE_TERM)));
    }

    @Test
    public void shouldUnescapeHtmlEntitiesWhenDocumentContainsThoseOnUpdate() throws CommunicationException,
                                                                                     JsonProcessingException {
        var updateItemResponse = mock(UpdateItemResponse.class);
        when(client.updateItem(any(UpdateItemRequest.class))).thenReturn(updateItemResponse);
        Map<String, AttributeValue> returnedItem = new HashMap<>();
        returnedItem.put(PRIMARYKEY_ISBN, AttributeValue.builder().s(SAMPLE_TERM).build());
        when(updateItemResponse.attributes()).thenReturn(returnedItem);
        String contents = IoUtils.stringFromResources(Path.of(GET_CONTENTS_JSON));
        ContentsDocument document = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        document = spy(document);
        doReturn("Title &#65;").when(document).getTitle();

        dbClient.updateContents(document);

        var captor = ArgumentCaptor.forClass(UpdateItemRequest.class);

        verify(client, times(1)).updateItem(captor.capture());

        var capturedTitle = captor.getValue().attributeUpdates().get("title").value().s();

        assertThat(capturedTitle, not(containsString("Title &#65;")));
        assertThat(capturedTitle, containsString("Title A"));
    }

}
