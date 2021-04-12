package no.unit.bibs.contents;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;


public class S3Client {

    private static final Logger logger = LoggerFactory.getLogger(S3Client.class);

    public static final String ERROR_UPLOADING_FILE = "Error uploading file";
    public static final String CANNOT_CONNECT_TO_S3 = "Cannot connect to S3";

    public static final String CONTENT_DISPOSITION_FILENAME_TEMPLATE = "filename=\"%s\"";

    private static final String AWS_REGION = "AWS_REGION";
    private static final String BUCKET_NAME = "BUCKET_NAME";

    @SuppressWarnings("PMD.UseUnderscoresInNumericLiterals")
    private static final int PRESIGNED_URL_EXPIRY_MILLISECONDS = 10000;

    private String bucketName;
    private AmazonS3 amazonS3Client;

    /**
     * Creates a new S3Client.
     */
    public S3Client(Environment environment) {
        initS3Client(environment);
    }

    /**
     * Creates a new S3Client.
     *
     * @param bucketName String
     */
    public S3Client(AmazonS3 amazonS3Client, String bucketName) {
        this.amazonS3Client = amazonS3Client;
        this.bucketName = bucketName;
    }

    @JacocoGenerated
    private void initS3Client(Environment environment) {
        try {
            this.amazonS3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(environment.readEnv(AWS_REGION))
                    .withPathStyleAccessEnabled(true)
                    .build();
            this.bucketName = environment.readEnv(BUCKET_NAME);
        } catch (Exception e) {
            logger.error(CANNOT_CONNECT_TO_S3, e);
        }
    }

    /**
     * Uploads inputstream to S3 using a presigned upload write url.
     *
     * @param inputStream inputStream
     * @param objectName objectName
     * @param filename filename
     * @param mimeType mimeType
     * @throws IOException IOException
     */
    @JacocoGenerated
    @SuppressWarnings("PMD.AssignmentInOperand")
    public void uploadFile(InputStream inputStream, String objectName, String filename, String mimeType)
            throws IOException {
        try {
            URL url = generatePresignedWriteUrl(objectName, filename, mimeType);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod(HttpMethod.PUT.name());
            connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, mimeType);
            connection.setRequestProperty(HttpHeaders.CONTENT_DISPOSITION,
                    String.format(CONTENT_DISPOSITION_FILENAME_TEMPLATE, filename));

            try (OutputStream outs = connection.getOutputStream()) {
                int numRead;
                byte[] buf = new byte[8 * 1024];
                while ((numRead = inputStream.read(buf)) >= 0) {
                    outs.write(buf, 0, numRead);
                }
                outs.flush();
                outs.close();
                // Check the HTTP response code. To complete the upload and make the object available,
                // you must interact with the connection object in some way.
                connection.getResponseCode();
            }
        } catch (IOException e) {
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
        Date expiration = new Date();
        long msec = expiration.getTime();
        msec += PRESIGNED_URL_EXPIRY_MILLISECONDS;
        expiration.setTime(msec);

        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName,
                objectName, HttpMethod.PUT);
        generatePresignedUrlRequest.setExpiration(expiration);

        if (filename != null && !filename.isEmpty()) {
            generatePresignedUrlRequest.addRequestParameter(HttpHeaders.CONTENT_DISPOSITION,
                    String.format(CONTENT_DISPOSITION_FILENAME_TEMPLATE, filename));
        }

        if (mimeType != null && !mimeType.isEmpty() && mimeType.contains("/")) {
            generatePresignedUrlRequest.addRequestParameter(HttpHeaders.CONTENT_TYPE, mimeType);
        }

        return amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest);
    }
}
