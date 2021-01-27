package no.unit.bibs.elasticsearch;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.unit.bibs.elasticsearch.exception.SearchException;
import no.unit.bibs.utils.ImportDataRequest;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.JsonUtils;
import nva.commons.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Objects.nonNull;

public class DataPipelineFileReaderIndexDocument {

    public static final String ERROR_ADDING_DOCUMENT_SEARCH_INDEX = "Error adding document with id={} to searchIndex";
    public static final String MANIFEST = "manifest";
    public static final String TOTAL_RECORDS_PROCESSED_IN_IMPORT_MESSAGE =
            "Total number of records processed in this import is {}";
    public static final String NUMBER_OF_IMPORTED_RECORDS_IN_THIS_FILE_MESSAGE =
            "Number of imported records in this file={}";
    public static final String READING_FROM_S3_MESSAGE = "Reading from s3://{}/{}";
    public static final String PUBLISHED = "published";

    private static final Logger logger = LoggerFactory.getLogger(DataPipelineFileReaderIndexDocument.class);
    private static final ObjectMapper mapper = JsonUtils.objectMapper;
    private final ElasticSearchHighLevelRestClient elasticSearchRestClient;
    private final AmazonS3 s3Client;

    public DataPipelineFileReaderIndexDocument(ElasticSearchHighLevelRestClient elasticSearchRestClient,
                                               AmazonS3 s3Client) {
        this.elasticSearchRestClient = elasticSearchRestClient;
        this.s3Client = s3Client;
    }


    /**
     * Reads one textfile with json dynamodb streamrecords one line at a time.
     *
     * @param s3Object BufferedReader containing json dynamodb records
     */
    @JacocoGenerated
    public Long readJsonDataFile(S3Object s3Object) {
        long indexedDocumentCount = 0;

        logger.debug(READING_FROM_S3_MESSAGE, s3Object.getBucketName(),s3Object.getKey());
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()))) {
            indexedDocumentCount = bufferedReader
                    .lines()
                    .filter(this::isPublishedPreliminaryCheck)
                    .map(this::fromJsonString)
                    .filter(Optional::isPresent)
                    .map(doc -> addDocumentToIndex(doc.get()))
                    .count();
            logger.info(NUMBER_OF_IMPORTED_RECORDS_IN_THIS_FILE_MESSAGE, indexedDocumentCount);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return indexedDocumentCount;
    }

    /**
     * Scans an S3 bucket with given key (folder) for files containing dynamodb json data files.
     *
     * @param importDataRequest Containing bucket and key for S3
     */
    public void scanS3Folder(ImportDataRequest importDataRequest) {

        AtomicLong counter = new AtomicLong(0L);

        ListObjectsV2Result listing =
                s3Client.listObjectsV2(importDataRequest.getS3bucket(), importDataRequest.getS3folderkey());

        listing.getObjectSummaries().stream()
                .filter(this::isDataFile)
                .map(this::getS3Object)
                .forEach(s3o -> counter.getAndAdd(readJsonDataFile(s3o)));

        logger.info(TOTAL_RECORDS_PROCESSED_IN_IMPORT_MESSAGE, counter.get());
    }

    protected S3Object getS3Object(S3ObjectSummary s3ObjectSummary) {
        return s3Client.getObject(new GetObjectRequest(s3ObjectSummary.getBucketName(), s3ObjectSummary.getKey()));
    }

    protected boolean isDataFile(S3ObjectSummary objectSummary) {
        return objectSummary.getSize() > 0 && !objectSummary.getKey().contains(MANIFEST);
    }

    protected boolean isPublishedPreliminaryCheck(String jsonSource) {
        return StringUtils.isNotEmpty(jsonSource) && jsonSource.toLowerCase(Locale.ROOT).contains(PUBLISHED);
    }

    private static boolean isPublished(JsonNode jsonNode) {
        var statusNode = jsonNode.get(IndexDocumentGenerator.STATUS);
        return nonNull(statusNode)
                && statusNode.toString().toLowerCase(Locale.US).contains(PUBLISHED.toLowerCase(Locale.US));
    }


    private Optional<IndexDocument> fromJsonString(String line) {
        try {
            JsonNode node = mapper.readTree(line);
            if (isPublished(node)) {
                var indexDocument = IndexDocumentGenerator.fromJsonNode(node);
                return Optional.of(indexDocument);
            }
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    private int addDocumentToIndex(IndexDocument document) {
        try {
            elasticSearchRestClient.addDocumentToIndex(document);
            return 1;
        } catch (SearchException e) {
            logger.error(ERROR_ADDING_DOCUMENT_SEARCH_INDEX, document.getId(), e);
            return 0;
        }
    }
}
