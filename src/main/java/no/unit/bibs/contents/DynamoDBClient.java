package no.unit.bibs.contents;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
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
     * Adds or insert a document to an elasticsearch index.
     * @param document the document to be inserted
     * @return the document added.
     * @throws CommunicationException when something goes wrong
     * */
    public String addContents(ContentsDocument document) throws CommunicationException {
        try {
            Item item = new Item()
                    .withString("isbn", document.getIsbn())
                    .withString("title", document.getTitle())
                    .withString("year", document.getYear())
                    .withString("author", document.getAuthor())
                    .withString("description_short", document.getDescriptionShort())
                    .withString("description_long", document.getDescriptionLong())
                    .withString("table_of_contents", document.getTableOfContents())
                    .withString("image_small", document.getImageSmall())
                    .withString("image_large", document.getImageLarge())
                    .withString("image_original", document.getImageOriginal())
                    .withString("audio_file", document.getAudioFile())
                    .withString("source", document.getSource())
                    .withString("created", Instant.now().toString());
            PutItemOutcome putItemOutcome = contentsTable.putItem(new PutItemSpec().withItem(item));
            return putItemOutcome.getItem().toJSON();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CommunicationException(e.getMessage(), e);
        }
    }

    /**
     * Gets the contentsDocument by given isbn.
     *
     * @param isbn identifier
     * @return contentsDocument as json string
     * @throws NotFoundException contentsDocument not found
     */
    public String getContents(String isbn) throws NotFoundException {
        Item item = contentsTable.getItem("isbn", isbn);
        if (Objects.isNull(item) || StringUtils.isEmpty(item.toJSON())) {
            throw new NotFoundException(String.format(DOCUMENT_WITH_ID_WAS_NOT_FOUND, isbn));
        }
        return item.toJSON();
    }

    public String updateContents(ContentsDocument document) throws CommunicationException {
        try {
            StringBuilder expression = new StringBuilder();
            ValueMap valueMap = new ValueMap();
            this.generateExpressionAndValueMap(document, expression, valueMap);
            UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("isbn", document.getIsbn());
            updateItemSpec = updateItemSpec.withUpdateExpression(expression.toString());
            updateItemSpec = updateItemSpec.withValueMap(valueMap);
            updateItemSpec = updateItemSpec.withReturnValues(ReturnValue.UPDATED_NEW);
            UpdateItemOutcome updateItemOutcome = contentsTable.updateItem(updateItemSpec);
            return updateItemOutcome.getItem().toJSON();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CommunicationException(e.getMessage(), e);
        }
    }

    private void generateExpressionAndValueMap(ContentsDocument document, StringBuilder expression,
                                               ValueMap valueMap) {
        Map<String, String> inputValues = this.findValuesToUpdate(document);
        expression.append("set ");
        for (String key : inputValues.keySet()) {
            expression.append(key).append(" = :").append(key).append(",");
            valueMap.withString(":" + key, inputValues.get(key));
        }
        // remove last bracket
        expression.deleteCharAt(expression.length() - 1);
    }

    private Map<String, String> findValuesToUpdate(ContentsDocument document) {
        Map<String, String> updateValueMap = new HashMap<>();
        if (StringUtils.isNotEmpty(document.getTitle())) {
            updateValueMap.put("title", document.getTitle());
        }
        if (StringUtils.isNotEmpty(document.getAuthor())) {
            updateValueMap.put("author", document.getAuthor());
        }
        if (StringUtils.isNotEmpty(document.getYear())) {
            updateValueMap.put("year", document.getYear());
        }
        if (StringUtils.isNotEmpty(document.getDescriptionShort())) {
            updateValueMap.put("description_short", document.getDescriptionShort());
        }
        if (StringUtils.isNotEmpty(document.getDescriptionLong())) {
            updateValueMap.put("description_long", document.getDescriptionLong());
        }
        if (StringUtils.isNotEmpty(document.getTableOfContents())) {
            updateValueMap.put("table_of_contents", document.getTableOfContents());
        }
        if (StringUtils.isNotEmpty(document.getImageSmall())) {
            updateValueMap.put("image_small", document.getImageSmall());
        }
        if (StringUtils.isNotEmpty(document.getImageLarge())) {
            updateValueMap.put("image_large", document.getImageLarge());
        }
        if (StringUtils.isNotEmpty(document.getImageOriginal())) {
            updateValueMap.put("image_original", document.getImageOriginal());
        }
        if (StringUtils.isNotEmpty(document.getAudioFile())) {
            updateValueMap.put("audio_file", document.getAudioFile());
        }
        updateValueMap.put("modified", Instant.now().toString());
        return updateValueMap;
    }
}
