package no.unit.bibs.contents;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;


public class S3Client {

    private static final Logger logger = LoggerFactory.getLogger(S3Client.class);

    public static final String ERROR_UPLOADING_FILE = "Error uploading file";
    public static final String ERROR_STORING_FILE = "error storing file: ";
    public static final String CANNOT_CONNECT_TO_S3 = "Cannot connect to S3";

    public static final String CONTENT_DISPOSITION_FILENAME_TEMPLATE = "filename=\"%s\"";

    public static final String ERROR_DOWNLOADING_FILE = "ISBN '%s' has invalid URL '%s' for file '%s' (%s): %s";
    public static final String OBJECT_KEY_TEMPLATE = "/%s/%s/%s/%s";
    public static final String FILE_NAME_TEMPLATE = "%s.%s";
    public static final String FILE_EXTENSION_JPG = "jpg";
    public static final String FILE_EXTENSION_MP3 = "mp3";
    public static final String SMALL = "small";
    public static final String LARGE = "large";
    public static final String ORIGINAL = "original";
    public static final String MP3 = "mp3";
    public static final String MIME_TYPE_IMAGE_JPG = "image/jpg";
    public static final String MIME_TYPE_AUDIO_MP3 = "audio/mpeg";
    public static final String HTTP_PREFIX = "http";

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
     * Uploads files found in ContentsDocument to S3 replacing url with s3 object key.
     *
     * @param contentsDocument contentsDocument
     * @throws IOException IOException
     */
    @JacocoGenerated
    public void handleFiles(ContentsDocument contentsDocument) {

        try {
            if (isDownloadableFile(contentsDocument.getImageSmall())) {
                String objectKey = putFileS3(
                        contentsDocument.getIsbn(),
                        contentsDocument.getImageSmall(),
                        SMALL,
                        FILE_EXTENSION_JPG,
                        MIME_TYPE_IMAGE_JPG
                );
                contentsDocument.setImageSmall(objectKey);
            }
        } catch (IOException e) {
            logger.error(ERROR_STORING_FILE + e.getMessage(), e);
        }

        try {
            if (isDownloadableFile(contentsDocument.getImageLarge())) {
                String objectKey = putFileS3(
                        contentsDocument.getIsbn(),
                        contentsDocument.getImageLarge(),
                        LARGE,
                        FILE_EXTENSION_JPG,
                        MIME_TYPE_IMAGE_JPG
                );
                contentsDocument.setImageLarge(objectKey);
            }
        } catch (IOException e) {
            logger.error(ERROR_STORING_FILE + e.getMessage(), e);
        }

        try {
            if (isDownloadableFile(contentsDocument.getImageOriginal())) {
                String objectKey = putFileS3(
                        contentsDocument.getIsbn(),
                        contentsDocument.getImageOriginal(),
                        ORIGINAL,
                        FILE_EXTENSION_JPG,
                        MIME_TYPE_IMAGE_JPG
                );
                contentsDocument.setImageOriginal(objectKey);
            }
        } catch (IOException e) {
            logger.error(ERROR_STORING_FILE + e.getMessage(), e);
        }

        try {
            if (isDownloadableFile(contentsDocument.getAudioFile())) {
                String objectKey = putFileS3(
                        contentsDocument.getIsbn(),
                        contentsDocument.getAudioFile(),
                        MP3,
                        FILE_EXTENSION_MP3,
                        MIME_TYPE_AUDIO_MP3
                );
                contentsDocument.setAudioFile(objectKey);
            }
        } catch (IOException e) {
            logger.error(ERROR_STORING_FILE + e.getMessage(), e);
        }
    }

    private boolean isDownloadableFile(String fileUrl) throws IOException {
        if (StringUtils.isNotEmpty(fileUrl) && fileUrl.startsWith(HTTP_PREFIX)) {
            URL url = new URL(fileUrl);
            HttpURLConnection.setFollowRedirects(true);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setRequestMethod(HttpMethod.HEAD.name());
            int responseCode = huc.getResponseCode();
            return responseCode < 300;
        }
        return false;
    }

    @JacocoGenerated
    private String putFileS3(String isbn, String url, String type, String fileExtension, String mimeType)
            throws IOException {

        String fileName = String.format(FILE_NAME_TEMPLATE, isbn, fileExtension);
        URL downloadUrl;
        try {
            downloadUrl = new URL(url);
        } catch (MalformedURLException e) {
            logger.error(String.format(ERROR_DOWNLOADING_FILE, isbn, url, fileName, type, e.getMessage()));
            throw e;
        }

        String secondLinkPart = isbn.substring(isbn.length() - 2, isbn.length() - 1);
        String firstLinkPart = isbn.substring(isbn.length() - 1);

        String objectKey = String.format(OBJECT_KEY_TEMPLATE, type, firstLinkPart, secondLinkPart, fileName);
        try (InputStream inputStream = downloadUrl.openStream()) {
            uploadFile(
                    inputStream,
                    objectKey,
                    fileName,
                    mimeType
            );
        }
        return objectKey;
    }

    /**
     * Uploads inputstream to S3 using a presigned upload write url.
     *
     * @param inputStream inputStream
     * @param objectName  objectName
     * @param filename    filename
     * @param mimeType    mimeType
     * @throws IOException IOException
     */
    @JacocoGenerated
    @SuppressWarnings("PMD.AssignmentInOperand")
    private void uploadFile(InputStream inputStream, String objectName, String filename, String mimeType)
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
