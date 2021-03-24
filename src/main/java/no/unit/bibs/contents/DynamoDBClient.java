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
import nva.commons.exceptions.commonexceptions.NotFoundException;
import nva.commons.utils.Environment;
import nva.commons.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
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

    private Table contentsTable;

    /**
     * Creates a new DynamoDBClient.
     *
     */
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
        item.withString(ContentsDocument.ISBN, document.getIsbn());
        conditionalAdd(item, document.getTitle(), ContentsDocument.TITLE);
        conditionalAdd(item, document.getDateOfPublication(), ContentsDocument.DATE_OF_PUBLICATION);
        conditionalAdd(item, document.getAuthor(), ContentsDocument.AUTHOR);
        conditionalAdd(item, document.getDescriptionShort(), ContentsDocument.DESCRIPTION_SHORT);
        conditionalAdd(item, document.getDescriptionLong(), ContentsDocument.DESCRIPTION_LONG);
        conditionalAdd(item, document.getTableOfContents(), ContentsDocument.TABLE_OF_CONTENTS);
        conditionalAdd(item, document.getImageSmall(), ContentsDocument.IMAGE_SMALL);
        conditionalAdd(item, document.getImageLarge(), ContentsDocument.IMAGE_LARGE);
        conditionalAdd(item, document.getImageOriginal(), ContentsDocument.IMAGE_ORIGINAL);
        conditionalAdd(item, document.getAudioFile(), ContentsDocument.AUDIO_FILE);
        item.withString(ContentsDocument.SOURCE, document.getSource());
        item.withString(ContentsDocument.CREATED, Instant.now().toString());
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
        Item item = contentsTable.getItem(ContentsDocument.ISBN, isbn);
        if (Objects.isNull(item) || StringUtils.isEmpty(item.toJSON())) {
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
    public String updateContents(ContentsDocument document) throws CommunicationException {
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
        conditionalAdd(updateValueMap, document.getTitle(), ContentsDocument.TITLE);
        conditionalAdd(updateValueMap, document.getAuthor(), ContentsDocument.AUTHOR);
        conditionalAdd(updateValueMap, document.getDateOfPublication(), ContentsDocument.DATE_OF_PUBLICATION);
        conditionalAdd(updateValueMap, document.getDescriptionShort(), ContentsDocument.DESCRIPTION_SHORT);
        conditionalAdd(updateValueMap, document.getDescriptionLong(), ContentsDocument.DESCRIPTION_LONG);
        conditionalAdd(updateValueMap, document.getTableOfContents(), ContentsDocument.TABLE_OF_CONTENTS);
        conditionalAdd(updateValueMap, document.getImageSmall(), ContentsDocument.IMAGE_SMALL);
        conditionalAdd(updateValueMap, document.getImageLarge(), ContentsDocument.IMAGE_LARGE);
        conditionalAdd(updateValueMap, document.getImageOriginal(), ContentsDocument.IMAGE_ORIGINAL);
        conditionalAdd(updateValueMap, document.getAudioFile(), ContentsDocument.AUDIO_FILE);
        updateValueMap.put(ContentsDocument.MODIFIED, Instant.now().toString());
        return updateValueMap;
    }

    private void conditionalAdd(Map<String, String> updateValueMap, String value, String title) {
        if (StringUtils.isNotEmpty(value)) {
            updateValueMap.put(title, value);
        }
    }

    private void conditionalAdd(Item item, String value, String title) {
        if (StringUtils.isNotEmpty(value)) {
            item.withString(title, value);
        }
    }
}
