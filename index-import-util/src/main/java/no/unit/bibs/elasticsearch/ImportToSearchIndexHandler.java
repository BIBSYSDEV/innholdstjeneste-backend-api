package no.unit.bibs.elasticsearch;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import no.unit.bibs.utils.ImportDataCreateResponse;
import no.unit.bibs.utils.ImportDataRequest;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.handlers.RestRequestHandler;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static java.util.Objects.isNull;

public class ImportToSearchIndexHandler extends ApiGatewayHandler<ImportDataRequest, ImportDataCreateResponse> {

    public static final String AWS_S3_BUCKET_REGION_KEY = "S3BUCKET_REGION";
    public static final String NO_PARAMETERS_GIVEN_TO_DATA_IMPORT_HANDLER = "No parameters given to DataImportHandler";
    public static final String CHECK_LOG_FOR_DETAILS_MESSAGE = "DataImport created, check log for details";
    private final DataPipelineFileReaderIndexDocument dynamoDBExportFileReader;

    @JacocoGenerated
    public ImportToSearchIndexHandler() {
        this(new Environment());
    }

    /**
     * Creating DataImportHandler from given environment.
     * @param environment for handler to operate in
     */
    public ImportToSearchIndexHandler(Environment environment) {
        this(environment, new ElasticSearchHighLevelRestClient(environment),
                AmazonS3ClientBuilder.standard()
                .withRegion(environment.readEnv(AWS_S3_BUCKET_REGION_KEY))
                .build());
    }

    /**
     * Creating DataImportHandler capable or reading dynamodb json datafiles on S3.
     * @param environment settings for application
     * @param elasticSearchClient Client speaking with elasticSearch
     * @param s3Client Client speaking with Amazon S3
     *
     */
    public ImportToSearchIndexHandler(Environment environment,
                                      ElasticSearchHighLevelRestClient elasticSearchClient,
                                      AmazonS3 s3Client) {
        super(ImportDataRequest.class, environment, LoggerFactory.getLogger(ImportToSearchIndexHandler.class));
        this.dynamoDBExportFileReader = new DataPipelineFileReaderIndexDocument(elasticSearchClient, s3Client);
    }

    /**
     * Implements the main logic of the handler. Any exception thrown by this method will be handled by {@link
     * RestRequestHandler#handleExpectedException} method.
     *
     * @param importDataRequest       The input object to the method. Usually a deserialized json.
     * @param requestInfo Request headers and path.
     * @param context     the ApiGateway context.ucket
     * @return the Response body that is going to be serialized in json
     * @throws ApiGatewayException all exceptions are caught by writeFailure and mapped to error codes through the
     *                             method {@link RestRequestHandler#getFailureStatusCode}
     */
    @Override
    protected ImportDataCreateResponse processInput(ImportDataRequest importDataRequest,
                                                    RequestInfo requestInfo,
                                                    Context context)
            throws ApiGatewayException {
        if (isNull(importDataRequest)) {
            throw new ImportException(NO_PARAMETERS_GIVEN_TO_DATA_IMPORT_HANDLER);
        }
        dynamoDBExportFileReader.scanS3Folder(importDataRequest);
        return new ImportDataCreateResponse(CHECK_LOG_FOR_DETAILS_MESSAGE,
                importDataRequest,
                HttpStatus.SC_ACCEPTED, Instant.now());
    }


    /**
     * Define the success status code.
     *
     * @param input  The request input.
     * @param output The response output
     * @return the success status code.
     */
    @Override
    protected Integer getSuccessStatusCode(ImportDataRequest input, ImportDataCreateResponse output) {
        return output.getStatusCode();
    }
}
