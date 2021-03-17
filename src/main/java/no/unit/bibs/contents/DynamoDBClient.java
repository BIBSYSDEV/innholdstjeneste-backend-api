package no.unit.bibs.contents;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import no.unit.bibs.contents.exception.CommunicationException;
import nva.commons.exceptions.commonexceptions.NotFoundException;
import nva.commons.utils.Environment;
import nva.commons.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Objects;


public class DynamoDBClient {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDBClient.class);

    public static final String DOCUMENT_WITH_ID_WAS_NOT_FOUND = "Document with id={} was not found.";
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
}
