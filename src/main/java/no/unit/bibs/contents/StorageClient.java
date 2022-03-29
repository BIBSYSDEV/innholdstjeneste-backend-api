package no.unit.bibs.contents;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageClient {

    private static final Logger logger = LoggerFactory.getLogger(StorageClient.class);

    public static final String ERROR_STORING_FILE = "error storing file: ";

    public static final String ERROR_DOWNLOADING_FILE = "ISBN '%s' has invalid URL '%s' for file '%s' (%s): %s";
    public static final String OBJECT_KEY_TEMPLATE = "files/%s/%s/%s/%s/%s";
    public static final String FILE_NAME_TEMPLATE = "%s.%s";
    public static final String FILE_EXTENSION_JPG = "jpg";
    public static final String FILE_EXTENSION_MP3 = "mp3";
    public static final String SMALL = "small";
    public static final String LARGE = "large";
    public static final String ORIGINAL = "original";
    public static final String MP3 = "mp3";
    public static final String AUDIO = "audio";
    public static final String IMAGES = "images";
    public static final String MIME_TYPE_IMAGE_JPG = "image/jpg";
    public static final String MIME_TYPE_AUDIO_MP3 = "audio/mpeg";
    public static final String HTTP_PREFIX = "http";

    private final S3Connection s3Connection;

    /**
     * Creates a new StorageClient.
     */
    public StorageClient(Environment environment) {
        s3Connection = new S3Connection(environment);
    }

    /**
     * Creates a new StorageClient.
     *
     * @param s3Connection s3Connection
     */
    public StorageClient(S3Connection s3Connection) {
        this.s3Connection = s3Connection;
    }

    private boolean isStringBase64Encoded(String input) {
        if (StringUtils.isNotEmpty(input)) {
            Pattern base64Pattern = Pattern.compile(
                "^(?:[A-Za-z0-9+\\/]{4})*(?:[A-Za-z0-9+\\/]{2}==|[A-Za-z0-9+\\/]{3}=|[A-Za-z0-9+\\/]{4})$");
            Matcher matcher = base64Pattern.matcher(input);
            return matcher.matches();
        } else {
            return false;
        }
    }

    /**
     * Decode Base64 encoded image file.
     *
     * @param isbn          isbn
     * @param input         contentsDocument imageSmall, imageNormal, imageLarge, audioFile
     * @param type          IMAGE or AUDIO
     * @param subtype       SMALL, LARGE, ORIGINAL, MP3
     * @param fileExtension jpg, mp3
     * @param mimeType      "image/jpg", "audio/mpeg
     * @return String objectKey
     */
    @JacocoGenerated
    private String decodeBase64Attributes(String isbn, String input, String type, String subtype,
                                                   String fileExtension, String mimeType) {
        return putFileS3(
                isbn,
                Base64.getDecoder().decode(input),
                type,
                subtype,
                fileExtension,
                mimeType);
    }

    /**
     * Send file to s3 storage.
     *
     * @param isbn          isbn
     * @param input         contentsDocument imageSmall, imageNormal, imageLarge, audioFile
     * @param type          IMAGE or AUDIO
     * @param subtype       SMALL, LARGE, ORIGINAL, MP3
     * @param fileExtension jpg, mp3
     * @param mimeType      "image/jpg", "audio/mpeg
     * @return String s3 objectKey
     */
    @JacocoGenerated
    private String sendToS3Bucket(String isbn, String input, String type, String subtype, String fileExtension,
                                  String mimeType) {
        if (StringUtils.isNotEmpty(input)) {
            if (isStringBase64Encoded(input)) {
                return decodeBase64Attributes(
                    isbn,
                    input,
                    type,
                    subtype,
                    fileExtension,
                    mimeType);
            } else {
                try {
                    if (isDownloadableFile(input)) {
                        return putFileS3(
                            isbn,
                            input,
                            type,
                            subtype,
                            fileExtension,
                            mimeType
                        );
                    }
                } catch (IOException e) {
                    logger.error(ERROR_STORING_FILE + e.getMessage(), e);
                }
            }
        }
        return null;
    }

    protected void updateContentDocumentWithObjectKey(ContentsDocument contentsDocument, String objectKey,
                                                      String subtype) {
        switch (subtype) {
            case SMALL:
                contentsDocument.setImageSmall(objectKey);
                break;
            case ORIGINAL:
                contentsDocument.setImageOriginal(objectKey);
                break;
            case LARGE:
                contentsDocument.setImageLarge(objectKey);
                break;
            case MP3:
                contentsDocument.setAudioFile(objectKey);
                break;
            default:
                break;
        }
    }

    /**
     * Uploads files found in ContentsDocument to S3 replacing url with s3 object key.
     *
     * @param contentsDocument contentsDocument
     */
    @JacocoGenerated
    public void handleFiles(ContentsDocument contentsDocument) {

        String imageSmall = contentsDocument.getImageSmall();
        String imageOriginal = contentsDocument.getImageOriginal();
        String imageLarge = contentsDocument.getImageLarge();

        if (StringUtils.isNotEmpty(imageSmall)) {
            String objectKey = sendToS3Bucket(
                contentsDocument.getIsbn(),
                imageSmall,
                IMAGES,
                SMALL,
                FILE_EXTENSION_JPG,
                MIME_TYPE_IMAGE_JPG
            );
            updateContentDocumentWithObjectKey(contentsDocument, objectKey, SMALL);
        }

        if (StringUtils.isNotEmpty(imageOriginal)) {
            String objectKey = sendToS3Bucket(
                contentsDocument.getIsbn(),
                imageOriginal,
                IMAGES,
                ORIGINAL,
                FILE_EXTENSION_JPG,
                MIME_TYPE_IMAGE_JPG
            );
            updateContentDocumentWithObjectKey(contentsDocument, objectKey, ORIGINAL);
        }

        if (StringUtils.isNotEmpty(imageLarge)) {
            String objectKey = sendToS3Bucket(contentsDocument.getIsbn(),
                imageLarge,
                IMAGES,
                LARGE,
                FILE_EXTENSION_JPG,
                MIME_TYPE_IMAGE_JPG
            );
            updateContentDocumentWithObjectKey(contentsDocument, objectKey, LARGE);
        }

        String audioFile = contentsDocument.getAudioFile();
        if (StringUtils.isNotEmpty(audioFile)) {
            String objectKey = sendToS3Bucket(contentsDocument.getIsbn(),
                audioFile,
                AUDIO,
                MP3,
                FILE_EXTENSION_MP3,
                MIME_TYPE_AUDIO_MP3
            );
            updateContentDocumentWithObjectKey(contentsDocument, objectKey, MP3);
        }
    }

    @JacocoGenerated
    private boolean isDownloadableFile(String fileUrl) throws IOException {
        if (StringUtils.isNotEmpty(fileUrl) && fileUrl.startsWith(HTTP_PREFIX)) {
            URL url = new URL(fileUrl);
            HttpURLConnection.setFollowRedirects(true);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setRequestMethod("HEAD");
            int responseCode = huc.getResponseCode();
            return responseCode < 300;
        }
        return false;
    }

    @JacocoGenerated
    protected String putFileS3(String isbn, byte[] bytesArray, String type, String subtype, String fileExtension,
                               String mimeType) {
        String fileName = String.format(FILE_NAME_TEMPLATE, isbn, fileExtension);

        String secondLinkPart = isbn.substring(isbn.length() - 2, isbn.length() - 1);
        String firstLinkPart = isbn.substring(isbn.length() - 1);

        String objectKey = String.format(OBJECT_KEY_TEMPLATE, type, subtype, firstLinkPart, secondLinkPart, fileName);
        s3Connection.uploadFile(
                bytesArray,
                objectKey,
                fileName,
                mimeType
        );
        return objectKey;
    }

    @JacocoGenerated
    protected String putFileS3(String isbn, String url, String type, String subtype, String fileExtension,
                               String mimeType) throws IOException {
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

        String objectKey = String.format(OBJECT_KEY_TEMPLATE, type, subtype, firstLinkPart, secondLinkPart, fileName);
        try (InputStream inputStream = downloadUrl.openStream()) {
            s3Connection.uploadFile(
                inputStream.readAllBytes(),
                objectKey,
                fileName,
                mimeType
            );
        }
        return objectKey;
    }
}
