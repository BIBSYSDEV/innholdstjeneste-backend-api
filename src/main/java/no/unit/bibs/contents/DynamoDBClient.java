package no.unit.bibs.contents;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.bibs.contents.exception.CommunicationException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;


public class DynamoDBClient {

    public static final Region REGION = Region.EU_WEST_1;
    private static final Logger logger = LoggerFactory.getLogger(DynamoDBClient.class);

    public static final String DOCUMENT_WITH_ID_WAS_NOT_FOUND = "Document with id=%s was not found.";
    public static final String CANNOT_CONNECT_TO_DYNAMO_DB = "Cannot connect to DynamoDB";
    public static final String TABLE_NAME = "TABLE_NAME";
    public static final String PRIMARYKEY_ISBN = "isbn";

    private static String tableName;
    private DynamoDbClient dbClient;

    /**
     * Creates a new DynamoDBClient.
     */
    @JacocoGenerated
    public DynamoDBClient(Environment environment) {
        initDynamoDbClient(environment);
    }

    /**
     * Creates a new DynamoDBClient.
     */
    public DynamoDBClient(DynamoDbClient dbClient) {
        this.dbClient = dbClient;
    }

    @JacocoGenerated
    private void initDynamoDbClient(Environment environment) {
        try {
            tableName = environment.readEnv(TABLE_NAME);
            dbClient = DynamoDbClient.builder().region(REGION).build();
        } catch (Exception e) {
            logger.error(CANNOT_CONNECT_TO_DYNAMO_DB, e);
        }
    }

    /**
     * Adds or insert a document to dynamoDB.
     *
     * @param document the document to be inserted
     * @throws CommunicationException when something goes wrong
     */
    public void createContents(ContentsDocument document) throws CommunicationException {
        try {
            PutItemRequest putItemRequest = PutItemRequest
                    .builder()
                    .tableName(tableName)
                    .item(this.generateItemMap(document))
                    .build();
            PutItemResponse putItemResponse = dbClient.putItem(putItemRequest);
            if (putItemResponse.hasAttributes()) {
                logger.info("contents created");
            } else {
                logger.error("Create contents went wrong. ");
                throw new RuntimeException("Create contents went wrong. ");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CommunicationException("Creation error: " + e.getMessage(), e);
        }
    }

    private Map<String, AttributeValue> generateItemMap(ContentsDocument document) {
        Map<String, AttributeValue> itemMap = new HashMap<>();
        itemMap.put(ContentsDocument.ISBN,
                AttributeValue.builder().s(document.getIsbn().toUpperCase(Locale.getDefault())).build());
        conditionalAddForCreate(itemMap, document.getTitle(), ContentsDocument.TITLE, true);
        conditionalAddForCreate(itemMap, document.getDateOfPublication(), ContentsDocument.DATE_OF_PUBLICATION, false);
        conditionalAddForCreate(itemMap, document.getAuthor(), ContentsDocument.AUTHOR, true);
        conditionalAddForCreate(itemMap, document.getDescriptionShort(), ContentsDocument.DESCRIPTION_SHORT, true);
        conditionalAddForCreate(itemMap, document.getDescriptionLong(), ContentsDocument.DESCRIPTION_LONG, true);
        conditionalAddForCreate(itemMap, document.getTableOfContents(), ContentsDocument.TABLE_OF_CONTENTS, true);
        conditionalAddForCreate(itemMap, document.getPromotional(), ContentsDocument.PROMOTIONAL, true);
        conditionalAddForCreate(itemMap, document.getSummary(), ContentsDocument.SUMMARY, true);
        conditionalAddForCreate(itemMap, document.getReview(), ContentsDocument.REVIEW, true);
        conditionalAddForCreate(itemMap, document.getImageSmall(), ContentsDocument.IMAGE_SMALL, false);
        conditionalAddForCreate(itemMap, document.getImageLarge(), ContentsDocument.IMAGE_LARGE, false);
        conditionalAddForCreate(itemMap, document.getImageOriginal(), ContentsDocument.IMAGE_ORIGINAL, false);
        conditionalAddForCreate(itemMap, document.getAudioFile(), ContentsDocument.AUDIO_FILE, false);
        itemMap.put(ContentsDocument.SOURCE, AttributeValue.builder().s(document.getSource()).build());
        if (Objects.isNull(document.getCreated())) {
            itemMap.put(ContentsDocument.CREATED, AttributeValue.builder().s(Instant.now().toString()).build());
        } else {
            itemMap.put(ContentsDocument.CREATED, AttributeValue.builder().s(document.getCreated().toString()).build());
        }
        return itemMap;
    }

    /**
     * Gets the contentsDocument by given isbn.
     *
     * @param isbn identifier
     * @return contentsDocument as json string
     * @throws NotFoundException contentsDocument not found
     */
    public String getContents(String isbn) throws NotFoundException {
        HashMap<String, AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put(PRIMARYKEY_ISBN, AttributeValue.builder().s(isbn).build());
        GetItemRequest request = GetItemRequest.builder()
                .key(keyToGet)
                .tableName(tableName)
                .build();
        try {
            GetItemResponse itemResponse = dbClient.getItem(request);
            if (itemResponse != null) {
                Map<String, AttributeValue> returnedItem = itemResponse.item();
                if (returnedItem != null && !returnedItem.isEmpty()) {
                    return parseAttributeValueMap(returnedItem);
                }
            }
            logger.info(String.format("No item found with the isbn %s!", isbn));
            throw new NotFoundException(String.format(DOCUMENT_WITH_ID_WAS_NOT_FOUND, isbn));
        } catch (DynamoDbException | JsonProcessingException e) {
            logger.error(e.getMessage());
            throw new NotFoundException(String.format(DOCUMENT_WITH_ID_WAS_NOT_FOUND, isbn));
        }
    }

    /**
     * Updates the contentsDocument identified by its isbn.
     *
     * @param document contentsDocument to update
     * @return json representation of contents.
     * @throws CommunicationException exception while connecting to database
     */
    protected String updateContents(ContentsDocument document) throws CommunicationException {
        try {
            Map<String, AttributeValueUpdate> attributeUpdates = this.findValuesToUpdate(document);
            UpdateItemRequest updateItemRequest = UpdateItemRequest
                    .builder()
                    .tableName(tableName)
                    .attributeUpdates(attributeUpdates)
                    .build();
            UpdateItemResponse updateItemResponse = dbClient.updateItem(updateItemRequest);
            logger.info("contents updated");
            Map<String, AttributeValue> returnedItem = updateItemResponse.attributes();
            if (returnedItem != null && !returnedItem.isEmpty()) {
                return parseAttributeValueMap(returnedItem);
            }
            logger.error("Update error: Could not find an item to return.");
            throw new RuntimeException("Update error: Could not find an item to return.");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CommunicationException("Update error: " + e.getMessage(), e);
        }
    }

    protected String parseAttributeValueMap(Map<String, AttributeValue> returnedItem) throws JsonProcessingException {
        Map<String, String> item = new HashMap<>();

        returnedItem.keySet()
                .forEach(key -> item.put(key,
                        returnedItem.get(key).getValueForField("S", String.class).orElse(null)));
        return dtoObjectMapper.writeValueAsString(item);
    }

    private Map<String, AttributeValueUpdate> findValuesToUpdate(ContentsDocument document) {
        Map<String, AttributeValueUpdate> updateValueMap = new HashMap<>();
        this.conditionalAddForUpdate(updateValueMap, document.getTitle(), ContentsDocument.TITLE, true);
        this.conditionalAddForUpdate(updateValueMap, document.getAuthor(), ContentsDocument.AUTHOR, true);
        this.conditionalAddForUpdate(updateValueMap, document.getDateOfPublication(),
                ContentsDocument.DATE_OF_PUBLICATION, false);
        this.conditionalAddForUpdate(updateValueMap, document.getDescriptionShort(), ContentsDocument.DESCRIPTION_SHORT,
                true);
        this.conditionalAddForUpdate(updateValueMap, document.getDescriptionLong(), ContentsDocument.DESCRIPTION_LONG,
                true);
        this.conditionalAddForUpdate(updateValueMap, document.getTableOfContents(), ContentsDocument.TABLE_OF_CONTENTS,
                true);
        this.conditionalAddForUpdate(updateValueMap, document.getPromotional(), ContentsDocument.PROMOTIONAL, true);
        this.conditionalAddForUpdate(updateValueMap, document.getSummary(), ContentsDocument.SUMMARY, true);
        this.conditionalAddForUpdate(updateValueMap, document.getReview(), ContentsDocument.REVIEW, true);
        this.conditionalAddForUpdate(updateValueMap, document.getImageSmall(), ContentsDocument.IMAGE_SMALL, false);
        this.conditionalAddForUpdate(updateValueMap, document.getImageLarge(), ContentsDocument.IMAGE_LARGE, false);
        this.conditionalAddForUpdate(updateValueMap, document.getImageOriginal(), ContentsDocument.IMAGE_ORIGINAL,
                false);
        this.conditionalAddForUpdate(updateValueMap, document.getAudioFile(), ContentsDocument.AUDIO_FILE, false);
        AttributeValue attributeValue = AttributeValue.builder().s(Instant.now().toString()).build();
        updateValueMap.put(ContentsDocument.MODIFIED, AttributeValueUpdate.builder().value(attributeValue).build());
        return updateValueMap;
    }

    protected void conditionalAddForUpdate(Map<String, AttributeValueUpdate> updateValueMap, String value, String key,
                                           boolean unescapeHtml) {
        if (StringUtils.isNotEmpty(value)) {
            String escaped = value;
            if (unescapeHtml && StringHelper.isValidHtmlEscapeCode(value)) {
                escaped = StringEscapeUtils.unescapeHtml4(value);
            }
            AttributeValue attributeValue = AttributeValue.builder().s(escaped).build();
            updateValueMap.put(key, AttributeValueUpdate.builder().value(attributeValue).build());
        }
    }

    protected void conditionalAddForCreate(Map<String, AttributeValue> itemMap, String value, String key,
                                           boolean unescapeHtml) {
        if (StringUtils.isNotEmpty(value)) {
            String escaped = value;
            if (unescapeHtml && StringHelper.isValidHtmlEscapeCode(value)) {
                escaped = StringEscapeUtils.unescapeHtml4(value);
            }
            itemMap.put(key, AttributeValue.builder().s(escaped).build());
        }
    }

}
