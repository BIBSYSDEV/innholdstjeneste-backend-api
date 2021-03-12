package no.unit.bibs.elasticsearch;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.unit.bibs.elasticsearch.exception.SearchException;
import nva.commons.utils.Environment;
import nva.commons.utils.JsonUtils;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class DynamoDBClient {


    private static final Logger logger = LoggerFactory.getLogger(DynamoDBClient.class);

    public static final String DOCUMENT_WITH_ID_WAS_NOT_FOUND_IN_ELASTICSEARCH
            = "Document with id={} was not found in elasticsearch";
    private static final ObjectMapper mapper = JsonUtils.objectMapper;

    private final String DYNAMODB_TABLE_NAME = "contents";
    private Table contentsTable;


    /**
     * Creates a new ElasticSearchRestClient.
     *
     * @param environment Environment with properties
     */
    public DynamoDBClient(Environment environment) {
        initDynamoDbClient();
    }

    /**
     * Creates a new ElasticSearchRestClient.
     *
     * @param environment Environment with properties
     * @param elasticSearchClient client to use for access to ElasticSearch
     */
    public DynamoDBClient(Environment environment, RestHighLevelClient elasticSearchClient) {
        initDynamoDbClient();
    }


    private void initDynamoDbClient() {
        AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.defaultClient();
        contentsTable = new DynamoDB(dynamoDB).getTable(DYNAMODB_TABLE_NAME);
    }

    /**
     * Adds or insert a document to an elasticsearch index.
     * @param document the document to be inserted
     * @throws SearchException when something goes wrong
     * */
    public void addDocumentToIndex(ContentsDocument document) throws SearchException {
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

    public QueryContentsResponse get(String isbn) throws SearchException {
        Item item = contentsTable.getItem("isbn", isbn);
        QueryContentsResponse.Builder builder = new QueryContentsResponse.Builder();
        List<JsonNode> hits = new ArrayList<>();
        try {
            hits.add(mapper.readTree(item.toJSON()));
        } catch (JsonProcessingException e) {
            throw new SearchException(DOCUMENT_WITH_ID_WAS_NOT_FOUND_IN_ELASTICSEARCH, e);
        }
        builder.withHits(hits);
        return new QueryContentsResponse(builder);
    }
}
