package no.unit.bibs.contents;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import no.unit.bibs.contents.exception.CommunicationException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


public class DynamoDBClient {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDBClient.class);

    public static final String DOCUMENT_WITH_ID_WAS_NOT_FOUND = "Document with id=%s was not found.";
    public static final String CANNOT_CONNECT_TO_DYNAMO_DB = "Cannot connect to DynamoDB";
    public static final String TABLE_NAME = "TABLE_NAME";
    public static final String KEY_PREFIX = ":";
    public static final String COMMA_ = ", ";
    public static final String _EQUALS_ = " = ";
    public static final String SET_ = "set ";
    private static final String EMPTY_JSON_OBJECT = "{}";

    private Table contentsTable;

    /**
     * Creates a new DynamoDBClient.
     *
     */
    @JacocoGenerated
    public DynamoDBClient(Environment environment)  {
        initDynamoDbClient(environment);
    }

    /**
     * Creates a new DynamoDBClient.
     *
     * @param dynamoTable Table
     */
    public DynamoDBClient(Table dynamoTable) {
        contentsTable = dynamoTable;
    }

    @JacocoGenerated
    private void initDynamoDbClient(Environment environment) {
        try {
            AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.defaultClient();
            contentsTable = new DynamoDB(dynamoDB).getTable(environment.readEnv(TABLE_NAME));
        } catch (Exception e) {
            logger.error(CANNOT_CONNECT_TO_DYNAMO_DB, e);
        }
    }

    /**
     * Adds or insert a document to dynamoDB.
     * @param document the document to be inserted
     * @throws CommunicationException when something goes wrong
     * */
    public void createContents(ContentsDocument document) throws CommunicationException {
        try {
            Item item = this.generateItem(document);
            contentsTable.putItem(new PutItemSpec().withItem(item));
            logger.info("contents created");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CommunicationException("Creation error: " + e.getMessage(), e);
        }
    }

    private Item generateItem(ContentsDocument document) {
        Item item = new Item();
        item.withString(ContentsDocument.ISBN, document.getIsbn().toUpperCase(Locale.getDefault()));
        conditionalAdd(item, document.getTitle(), ContentsDocument.TITLE, true);
        conditionalAdd(item, document.getDateOfPublication(), ContentsDocument.DATE_OF_PUBLICATION, false);
        conditionalAdd(item, document.getAuthor(), ContentsDocument.AUTHOR, true);
        conditionalAdd(item, document.getDescriptionShort(), ContentsDocument.DESCRIPTION_SHORT, true);
        conditionalAdd(item, document.getDescriptionLong(), ContentsDocument.DESCRIPTION_LONG, true);
        conditionalAdd(item, document.getTableOfContents(), ContentsDocument.TABLE_OF_CONTENTS, true);
        conditionalAdd(item, document.getPromotional(), ContentsDocument.PROMOTIONAL, true);
        conditionalAdd(item, document.getSummary(), ContentsDocument.SUMMARY, true);
        conditionalAdd(item, document.getReview(), ContentsDocument.REVIEW, true);
        conditionalAdd(item, document.getImageSmall(), ContentsDocument.IMAGE_SMALL, false);
        conditionalAdd(item, document.getImageLarge(), ContentsDocument.IMAGE_LARGE, false);
        conditionalAdd(item, document.getImageOriginal(), ContentsDocument.IMAGE_ORIGINAL, false);
        conditionalAdd(item, document.getAudioFile(), ContentsDocument.AUDIO_FILE, false);
        item.withString(ContentsDocument.SOURCE, document.getSource());
        if (Objects.isNull(document.getCreated())) {
            item.withString(ContentsDocument.CREATED, Instant.now().toString());
        } else {
            item.withString(ContentsDocument.CREATED, document.getCreated().toString());
        }
        return item;
    }


    /**
     * Gets the contentsDocument by given isbn.
     *
     * @param isbn identifier
     * @return contentsDocument as json string
     * @throws NotFoundException contentsDocument not found
     */
    public String getContents(String isbn) throws NotFoundException {
        Item item = contentsTable.getItem(ContentsDocument.ISBN, isbn.toUpperCase(Locale.getDefault()));
        System.out.println("Must have found an Item: " + item);
        if (Objects.isNull(item) || EMPTY_JSON_OBJECT.equals(item.toJSON())) {
            System.out.println("Item was empty eller null");
            throw new NotFoundException(String.format(DOCUMENT_WITH_ID_WAS_NOT_FOUND, isbn));
        }
        return item.toJSON();
    }

    /**
     * Updates the contentsDocument identified by it's isbn.
     * @param document contentsDocument to update
     * @return json representation of contents.
     * @throws CommunicationException exception while connecting to database
     */
    protected String updateContents(ContentsDocument document) throws CommunicationException {
        try {
            StringBuilder expression = new StringBuilder();
            ValueMap valueMap = new ValueMap();
            this.generateExpressionAndValueMap(document, expression, valueMap);
            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                    .withPrimaryKey(ContentsDocument.ISBN, document.getIsbn());
            updateItemSpec = updateItemSpec.withUpdateExpression(expression.toString());
            updateItemSpec = updateItemSpec.withValueMap(valueMap);
            updateItemSpec = updateItemSpec.withReturnValues(ReturnValue.UPDATED_NEW);
            UpdateItemOutcome updateItemOutcome = contentsTable.updateItem(updateItemSpec);
            logger.info("contents updated");
            return updateItemOutcome.getItem().toJSON();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CommunicationException("Update error: " + e.getMessage(), e);
        }
    }

    private void generateExpressionAndValueMap(ContentsDocument document, StringBuilder expression,
                                               ValueMap valueMap) {
        Map<String, String> inputValues = this.findValuesToUpdate(document);
        expression.append(SET_);
        for (String key : inputValues.keySet()) {
            expression.append(key).append(_EQUALS_).append(KEY_PREFIX).append(key).append(COMMA_);
            valueMap.withString(KEY_PREFIX + key, inputValues.get(key));
        }
        // remove last bracket
        expression.deleteCharAt(expression.length() - COMMA_.length());
    }

    private Map<String, String> findValuesToUpdate(ContentsDocument document) {
        Map<String, String> updateValueMap = new HashMap<>();
        conditionalAdd(updateValueMap, document.getTitle(), ContentsDocument.TITLE, true);
        conditionalAdd(updateValueMap, document.getAuthor(), ContentsDocument.AUTHOR, true);
        conditionalAdd(updateValueMap, document.getDateOfPublication(), ContentsDocument.DATE_OF_PUBLICATION, false);
        conditionalAdd(updateValueMap, document.getDescriptionShort(), ContentsDocument.DESCRIPTION_SHORT, true);
        conditionalAdd(updateValueMap, document.getDescriptionLong(), ContentsDocument.DESCRIPTION_LONG, true);
        conditionalAdd(updateValueMap, document.getTableOfContents(), ContentsDocument.TABLE_OF_CONTENTS, true);
        conditionalAdd(updateValueMap, document.getPromotional(), ContentsDocument.PROMOTIONAL, true);
        conditionalAdd(updateValueMap, document.getSummary(), ContentsDocument.SUMMARY, true);
        conditionalAdd(updateValueMap, document.getReview(), ContentsDocument.REVIEW, true);
        conditionalAdd(updateValueMap, document.getImageSmall(), ContentsDocument.IMAGE_SMALL, false);
        conditionalAdd(updateValueMap, document.getImageLarge(), ContentsDocument.IMAGE_LARGE, false);
        conditionalAdd(updateValueMap, document.getImageOriginal(), ContentsDocument.IMAGE_ORIGINAL, false);
        conditionalAdd(updateValueMap, document.getAudioFile(), ContentsDocument.AUDIO_FILE, false);
        updateValueMap.put(ContentsDocument.MODIFIED, Instant.now().toString());
        return updateValueMap;
    }

    protected void conditionalAdd(Map<String, String> updateValueMap, String value, String key, boolean unescapeHtml) {
        if (StringUtils.isNotEmpty(value)) {
            String escaped = value;
            if (unescapeHtml && StringHelper.isValidHtmlEscapeCode(value)) {
                escaped = StringEscapeUtils.unescapeHtml4(value);
            }
            updateValueMap.put(key, escaped);
        }
    }

    protected void conditionalAdd(Item item, String value, String key, boolean unescapeHtml) {
        if (StringUtils.isNotEmpty(value)) {
            String escaped = value;
            if (unescapeHtml && StringHelper.isValidHtmlEscapeCode(value)) {
                escaped = StringEscapeUtils.unescapeHtml4(value);
            }
            item.withString(key, escaped);
        }
    }

}
