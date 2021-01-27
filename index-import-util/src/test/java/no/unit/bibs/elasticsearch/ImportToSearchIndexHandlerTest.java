package no.unit.bibs.elasticsearch;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import no.unit.bibs.utils.ImportDataCreateResponse;
import no.unit.bibs.utils.ImportDataRequest;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.log.LogUtils;
import nva.commons.utils.log.TestAppender;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.Instant;

import static no.unit.bibs.elasticsearch.ImportToSearchIndexHandler.AWS_S3_BUCKET_REGION_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ImportToSearchIndexHandlerTest {

    public static final String ELASTICSEARCH_ENDPOINT_ADDRESS = "localhost";
    private static final String SAMPLE_BUCKET_NAME = "nva-datapipeline";
    private static final String SAMPLE_S3FOLDER_KEY = "2020-10-12-06-55-32";
    private static final String SAMPLE_S3REGION = "eu-west-1";
    private static final String ELASTICSEARCH_ENDPOINT_INDEX = "resources";


    private Context mockContext = mock(Context.class);
    private Environment mockEnvironment = setupMockEnvironment();
    private TestAppender testAppender;


    private Environment setupMockEnvironment() {
        Environment environment = mock(Environment.class);
        doReturn(ELASTICSEARCH_ENDPOINT_ADDRESS).when(environment)
                .readEnv(ElasticSearchHighLevelRestClient.ELASTICSEARCH_ENDPOINT_ADDRESS_KEY);
        doReturn(ELASTICSEARCH_ENDPOINT_INDEX).when(environment)
                .readEnv(ElasticSearchHighLevelRestClient.ELASTICSEARCH_ENDPOINT_INDEX_KEY);
        doReturn(SAMPLE_S3REGION).when(environment)
                .readEnv(AWS_S3_BUCKET_REGION_KEY);
        return environment;
    }


    ImportToSearchIndexHandler setupMockHandler() {

        ElasticSearchHighLevelRestClient mockElasticSearchClient = mock(ElasticSearchHighLevelRestClient.class);
        AmazonS3 mockS3Client = mock(AmazonS3.class);
        ListObjectsV2Result listing = mock(ListObjectsV2Result.class);
        when(mockS3Client.listObjectsV2(anyString(), anyString())).thenReturn(listing);
        when(mockS3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listing);
        ImportToSearchIndexHandler handler = new ImportToSearchIndexHandler(mockEnvironment,
                mockElasticSearchClient,
                mockS3Client);
        testAppender = LogUtils.getTestingAppender(ImportToSearchIndexHandler.class);
        return handler;
    }


    @Test
    void handlerAcceptsSampleImportRequest() throws ApiGatewayException {
        ImportDataRequest importDataRequest = getImportDataRequest();
        setupMockHandler().processInput(importDataRequest, new RequestInfo(), mockContext);

    }

    @Test
    void handlerThrowsExceptionWhenInputIsBad() {
        Executable executable = () -> setupMockHandler().processInput(null, null, mockContext);
        assertThrows(ApiGatewayException.class, executable);
    }

    @Test
    void constructorWithEnvironmentCreatesHandler() {
        ImportToSearchIndexHandler handler = new ImportToSearchIndexHandler(mockEnvironment);
        assertNotNull(handler);
    }

    @Test
    void getSuccessStatusCodeReturnsCodeFromResponse() {
        int imATeaPot = 418;
        ImportDataCreateResponse response = new ImportDataCreateResponse("",
                getImportDataRequest(),
                imATeaPot,
                Instant.now());
        Integer statusCode = setupMockHandler().getSuccessStatusCode(null, response);
        assertEquals(statusCode, imATeaPot);
    }


    @Test
    void handlerReturnsAcceptedWhenInputIsOK() throws ApiGatewayException {

        ImportDataRequest importDataRequest = getImportDataRequest();

        ImportDataCreateResponse response = setupMockHandler().processInput(importDataRequest,
                new RequestInfo(),
                mockContext);

        ImportDataCreateResponse expected = new ImportDataCreateResponse(response.getMessage(),
                importDataRequest,
                HttpStatus.SC_ACCEPTED,
                response.getTimestamp());

        assertEquals(expected,response);
    }

    private ImportDataRequest getImportDataRequest() {
        return new ImportDataRequest.Builder()
                .withS3Bucket(SAMPLE_BUCKET_NAME)
                .withS3FolderKey(SAMPLE_S3FOLDER_KEY)
                .build();
    }

}