package no.unit.bibs.contents;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

public class S3Connection {

    private static final Logger logger = LoggerFactory.getLogger(S3Connection.class);

    public static final String ERROR_UPLOADING_FILE = "Error uploading file";
    public static final String CONTENT_DISPOSITION_FILENAME_TEMPLATE = "filename=\"%s\"";
    @SuppressWarnings("PMD.UseUnderscoresInNumericLiterals")
    private static final int PRESIGNED_URL_EXPIRY_MILLISECONDS = 10000;
    private String bucketName;
    private S3Presigner s3Presigner;
    private S3Client s3Client;
    private static final String AWS_REGION = "AWS_REGION";
    private static final String BUCKET_NAME = "BUCKET_NAME";
    public static final String CANNOT_CONNECT_TO_S3 = "Cannot connect to S3";

    public S3Connection(Environment environment) {
        initS3Client(environment);
    }

    /**
     * Constructor for use in test to inject.
     * @param s3Client aws S3Client
     * @param s3Presigner aws S3 presigner
     * @param bucketName name of s3 bucket
     */
    public S3Connection(S3Client s3Client, S3Presigner s3Presigner, String bucketName) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
    }

    @JacocoGenerated
    private void initS3Client(Environment environment) {
        try {
            this.s3Client = S3Client.builder()
                    .region(Region.of(environment.readEnv(AWS_REGION)))
                    .build();
            this.s3Presigner = S3Presigner.builder()
                    .region(Region.of(environment.readEnv(AWS_REGION)))
                    .build();
            this.bucketName = environment.readEnv(BUCKET_NAME);
        } catch (Exception e) {
            logger.error(CANNOT_CONNECT_TO_S3, e);
        }
    }

    /**
     * Uploads inputstream to S3 using a presigned upload write url.
     *
     * @param bytesArray bytesArray
     * @param objectName  objectName
     * @param filename    filename
     * @param mimeType    mimeType
     */
    @JacocoGenerated
    @SuppressWarnings("PMD.AssignmentInOperand")
    protected void uploadFile(byte[] bytesArray, String objectName, String filename, String mimeType) {
        try {
            PutObjectRequest putObjectRequest = createPutObjectRequest(objectName, filename, mimeType);
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytesArray));
        } catch (S3Exception e) {
            logger.error(ERROR_UPLOADING_FILE, e);
            throw e;
        }
    }

    /**
     * Generate a pre-signed WRITE URL for an object.
     *
     * @param objectName String
     * @param filename   String
     * @param mimeType   String
     * @return URL
     */
    public URL generatePresignedWriteUrl(String objectName, String filename, String mimeType) {

        PutObjectRequest putObjectRequest = createPutObjectRequest(objectName, filename, mimeType);

        PutObjectPresignRequest presignedPutObjectRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMillis(PRESIGNED_URL_EXPIRY_MILLISECONDS))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignedPutObjectRequest);

        return presignedRequest.url();
    }

    private PutObjectRequest createPutObjectRequest(String objectName, String filename, String mimeType) {

        Map<String, String> metadata = new HashMap<>();
        if (filename != null && !filename.isEmpty()) {
            metadata.put(HttpHeaders.CONTENT_DISPOSITION,
                    String.format(CONTENT_DISPOSITION_FILENAME_TEMPLATE, filename));
        }

        if (mimeType != null && !mimeType.isEmpty() && mimeType.contains("/")) {
            metadata.put(HttpHeaders.CONTENT_TYPE, mimeType);
        }

        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .metadata(metadata)
                .build();
    }

}
