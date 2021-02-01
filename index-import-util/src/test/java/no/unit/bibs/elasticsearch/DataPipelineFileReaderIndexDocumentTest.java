package no.unit.bibs.elasticsearch;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import no.unit.bibs.utils.ImportDataRequest;
import nva.commons.utils.Environment;
import nva.commons.utils.IoUtils;
import nva.commons.utils.log.LogUtils;
import nva.commons.utils.log.TestAppender;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataPipelineFileReaderIndexDocumentTest {
    public static final String ELASTICSEARCH_ENDPOINT_ADDRESS = "localhost";
    private static final String ELASTICSEARCH_ENDPOINT_INDEX = "resources";

    private static final String SAMPLE_BUCKET_NAME = "nva-datapipeline";
    private static final String SAMPLE_S3FOLDER_KEY = "2020-10-12-06-55-32";
    private static final String SAMPLE_DATAPIPELINE_OUTPUT_FILE = "datapipeline_output_sample";
    private static final String DUMMY_FILE_NAME = "some_random_file_name";
    public static final long THE_ANSWER_TO_EVERYTHING = 42L;
    private static final String TOTAL_RECORDS_PROCESSED_MESSAGE =
            "Total number of records processed in this import is 11";

    private ElasticSearchHighLevelRestClient mockElasticSearchClient;
    private AmazonS3 mockS3Client;
    private ListObjectsV2Result mockListObjectsV2Result;
    private S3ObjectSummary mockS3ObjectSummary;

    private Environment setupMockEnvironment() {
        Environment environment = mock(Environment.class);
        doReturn(ELASTICSEARCH_ENDPOINT_ADDRESS).when(environment)
                .readEnv(ElasticSearchHighLevelRestClient.ELASTICSEARCH_ENDPOINT_ADDRESS_KEY);
        doReturn(ELASTICSEARCH_ENDPOINT_INDEX).when(environment)
                .readEnv(ElasticSearchHighLevelRestClient.ELASTICSEARCH_ENDPOINT_INDEX_KEY);
        return environment;
    }

    private void initMocking() {
        setupMockEnvironment();

        mockElasticSearchClient = mock(ElasticSearchHighLevelRestClient.class);
        mockS3Client = mock(AmazonS3.class);
        mockS3ObjectSummary = mock(S3ObjectSummary.class);
        mockListObjectsV2Result = mock(ListObjectsV2Result.class);

        List<S3ObjectSummary> objectSummaries = new ArrayList<>();
        objectSummaries.add(mockS3ObjectSummary);

        when(mockS3ObjectSummary.getSize()).thenReturn(THE_ANSWER_TO_EVERYTHING);
        when(mockS3ObjectSummary.getKey()).thenReturn(DUMMY_FILE_NAME);
        when(mockListObjectsV2Result.getObjectSummaries()).thenReturn(objectSummaries);
        when(mockS3Client.listObjectsV2(anyString(), anyString())).thenReturn(mockListObjectsV2Result);
        when(mockS3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(mockListObjectsV2Result);

        S3Object mockS3Object = mock(S3Object.class);
        when(mockS3Client.getObject(any())).thenReturn(mockS3Object);

        InputStream inputStream = IoUtils.inputStreamFromResources(SAMPLE_DATAPIPELINE_OUTPUT_FILE);
        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(inputStream);
        when(mockS3Object.getObjectContent()).thenReturn(s3Object.getObjectContent());
    }

    private TestAppender createAppenderForLogMonitoring() {
        return LogUtils.getTestingAppender(DataPipelineFileReaderIndexDocument.class);
    }


    @Test
    void scanS3FolderLogsExportedRecordsWhenDynamodDBFileExportReaderIsCorrectlyConfigured() throws IOException {
        initMocking();
        final TestAppender appender = createAppenderForLogMonitoring();
        DataPipelineFileReaderIndexDocument exportFileReader;
        exportFileReader = new DataPipelineFileReaderIndexDocument(mockElasticSearchClient, mockS3Client);
        ImportDataRequest importDataRequest = new ImportDataRequest.Builder()
                .withS3Bucket(SAMPLE_BUCKET_NAME)
                .withS3FolderKey(SAMPLE_S3FOLDER_KEY)
                .build();
        exportFileReader.scanS3Folder(importDataRequest);

        assertThat(appender.getMessages(), containsString(TOTAL_RECORDS_PROCESSED_MESSAGE));
    }


    @Test
    void addingDocumentToIndexHidesExceptionAndWritesLogWhenInputHasErrors() throws SearchException {

        initMocking();

        ElasticSearchHighLevelRestClient highLevelRestClient = mock(ElasticSearchHighLevelRestClient.class);
        doThrow(SearchException.class).when(highLevelRestClient).addDocumentToIndex(any());

        mockElasticSearchClient = mock(ElasticSearchHighLevelRestClient.class);
        InputStream inputStream = IoUtils.inputStreamFromResources(SAMPLE_DATAPIPELINE_OUTPUT_FILE);

        DataPipelineFileReaderIndexDocument exportFileReader;
        exportFileReader = new DataPipelineFileReaderIndexDocument(highLevelRestClient, mockS3Client);

        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(inputStream);
        exportFileReader.readJsonDataFile(s3Object);
    }

    @Test
    void datafileFilterFiltersOutStatusfilesWhenStatusfilesExistsInFolder() throws SearchException {
        initMocking();
        DataPipelineFileReaderIndexDocument exportFileReader;
        exportFileReader = new DataPipelineFileReaderIndexDocument(mockElasticSearchClient, mockS3Client);
        assertTrue(exportFileReader.isDataFile(mockS3ObjectSummary));
    }

    @Test
    void getS3ObjectReturnsS3ObjectWhenS3ObjectExists() throws SearchException {
        initMocking();
        DataPipelineFileReaderIndexDocument exportFileReader;
        exportFileReader = new DataPipelineFileReaderIndexDocument(mockElasticSearchClient, mockS3Client);
        assertNotNull(exportFileReader.getS3Object(mockS3ObjectSummary));
    }

}
