package no.unit.bibs.contents.document;

import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * DocumentClient is responsible for interacting with the database to perform operations
 * related to documents, such as fetching, updating, and creating documents.
 */
@JacocoGenerated
public class DocumentClient {


    /* package */
    static final String AWS_REGION_ENV = "AWS_REGION";
    /* package */
    static final String TABLE_NAME_ENV = "TABLE_NAME";

    private final DynamoDbTable<DocumentDao> customerTable;

    public DocumentClient() {
        this(new Environment());
    }

    public DocumentClient(Environment environment) {
        this(
            environment.readEnv(TABLE_NAME_ENV),
            environment.readEnv(AWS_REGION_ENV)
        );
    }

    public DocumentClient(String tableName, String awsRegion) {
        var enhancedClient = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(DynamoDbClient.builder()
                .region(Region.of(awsRegion))
                .build())
            .build();
        customerTable = enhancedClient.table(tableName, TableSchema.fromBean(DocumentDao.class));
    }

    public DocumentDao getByIsbn(String isbn) {
        return customerTable.getItem(
            DocumentDao.builder()
                .isbn(isbn)
                .build()
        );
    }

    public DocumentDao update(DocumentDao document) {
        return customerTable.updateItem(document);
    }

    public DocumentDao create(DocumentDao document) {
        return customerTable.updateItem(document);
    }
}
