package no.unit.bibs.contents;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.unit.bibs.contents.exception.CommunicationException;
import no.unit.bibs.contents.exception.SearchException;
import nva.commons.exceptions.commonexceptions.NotFoundException;
import nva.commons.utils.Environment;
import nva.commons.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class DynamoDBClient {


    private static final Logger logger = LoggerFactory.getLogger(DynamoDBClient.class);

    public static final String DOCUMENT_WITH_ID_WAS_NOT_FOUND
            = "Document with id={} was not found.";
    private static final ObjectMapper mapper = JsonUtils.objectMapper;
    public static final String CANNOT_CONNECT_TO_DYNAMO_DB = "Cannot connect to DynamoDB";

    private final String DYNAMODB_TABLE_NAME = "contents";
    private Table contentsTable;


    /**
     * Creates a new DynamoDBClient.
     *
     */
    public DynamoDBClient() throws CommunicationException {
        initDynamoDbClient();
    }

    /**
     * Creates a new DynamoDBClient.
     *
     * @param environment Environment with properties
     */
    public DynamoDBClient(Environment environment) throws CommunicationException {
        initDynamoDbClient();
    }
    /**
     * Creates a new DynamoDBClient.
     *
     * @param dynamoTable Table
     */
    public DynamoDBClient(Table dynamoTable) {
        contentsTable = dynamoTable;
    }


    private void initDynamoDbClient() throws CommunicationException {
        try {
            AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.defaultClient();
            contentsTable = new DynamoDB(dynamoDB).getTable(DYNAMODB_TABLE_NAME);
        } catch (Exception e) {
            throw new CommunicationException(CANNOT_CONNECT_TO_DYNAMO_DB, e);
        }
    }

    /**
     * Adds or insert a document to an elasticsearch index.
     * @param document the document to be inserted
     * @throws SearchException when something goes wrong
     * */
    public void addDocument(ContentsDocument document) throws SearchException {
        try {
            Item item = new Item()
                    .withString("isbn", document.getIsbn())
                    .withString("source", document.getSource())
                    .withString("created", Instant.now().toString());
            contentsTable.putItem(new PutItemSpec().withItem(item));
        } catch (Exception e) {
            throw new SearchException(e.getMessage(), e);
        }

    }

    public QueryContentsResponse get(String isbn) throws NotFoundException {
        Item item = contentsTable.getItem("isbn", isbn);
        QueryContentsResponse.Builder builder = new QueryContentsResponse.Builder();
        List<JsonNode> hits = new ArrayList<>();
        try {
            hits.add(mapper.readTree(item.toJSON()));
        } catch (Exception e) {
            throw new NotFoundException(e, DOCUMENT_WITH_ID_WAS_NOT_FOUND);
        }
        builder.withHits(hits);
        return new QueryContentsResponse(builder);
    }
}
