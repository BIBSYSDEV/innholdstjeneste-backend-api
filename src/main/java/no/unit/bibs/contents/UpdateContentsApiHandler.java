package no.unit.bibs.contents;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.bibs.contents.exception.ParameterException;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.exceptions.commonexceptions.NotFoundException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.handlers.RestRequestHandler;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static java.util.Objects.isNull;

public class UpdateContentsApiHandler extends ApiGatewayHandler<ContentsRequest, GatewayResponse> {

    public static final String OBJECT_KEY_TEMPLATE = "/%s/%s/%s/%s";
    public static final String FILE_NAME_TEMPLATE = "%s.%s";
    public static final String FILE_EXTENSION_JPG = "jpg";
    public static final String FILE_EXTENSION_WAV = "wav";
    public static final String NO_PARAMETERS_GIVEN_TO_HANDLER = "No parameters given to UpdateContentsApiHandler";
    public static final String COULD_NOT_UPDATE_PROVIDED_CONTENTS = "Could not update provided contents. ";
    public static final String CONTENTS_CREATED = "contents created";
    public static final String CONTENTS_UPDATED = "contents updated";
    public static final String FAILED_AFTER_PERSISTING = "failed after persisting: ";
    public static final String ERROR_IN_UPDATE_FUNCTION = "error in update function: ";
    public static final String ERROR_DOWNLOADING_FILE = "ISBN '%s' has invalid URL '%s' for file '%s' (%s): %s";
    public static final String ERROR_STORING_FILE = "error storing file: ";
    public static final String THIS_IS_MY_CONTENTS_DOCUMENT_TO_PERSIST = "This is my ContentsDocument to persist: ";
    public static final String JSON_INPUT_LOOKS_LIKE_THAT = "json input looks like that :";
    public static final int HALF_A_SECOND = 500;
    public static final String SMALL = "small";
    public static final String LARGE = "large";
    public static final String ORIGINAL = "original";
    public static final String AUDIO = "audio";
    public static final String MIME_TYPE_IMAGE_JPG = "image/jpg";
    public static final String MIME_TYPE_AUDIO_WAV = "audio/wav";
    public static final String HTTP_PREFIX = "http";

    private final DynamoDBClient dynamoDBClient;
    private final S3Client s3Client;

    @JacocoGenerated
    public UpdateContentsApiHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public UpdateContentsApiHandler(Environment environment) {
        this(environment, new DynamoDBClient(environment), new S3Client(environment));
    }

    public UpdateContentsApiHandler(Environment environment, DynamoDBClient dynamoDBClient, S3Client s3Client) {
        super(ContentsRequest.class, environment, LoggerFactory.getLogger(UpdateContentsApiHandler.class));
        this.dynamoDBClient = dynamoDBClient;
        this.s3Client = s3Client;
    }


    /**
     * Implements the main logic of the handler. Any exception thrown by this method will be handled by {@link
     * RestRequestHandler#handleExpectedException} method.
     *
     * @param request     The input object to the method. Usually a deserialized json.
     * @param requestInfo Request headers and path.
     * @param context     the ApiGateway context.
     * @return the Response body that is going to be serialized in json
     * @throws ApiGatewayException all exceptions are caught by writeFailure and mapped to error codes through the
     *                             method {@link RestRequestHandler#getFailureStatusCode}
     */
    @Override
    protected GatewayResponse processInput(ContentsRequest request, RequestInfo requestInfo,
                                           Context context) throws ApiGatewayException {
        if (isNull(request)) {
            throw new ParameterException(NO_PARAMETERS_GIVEN_TO_HANDLER);
        }
        ContentsDocument contentsDocument = request.getContents();
        logger.info(JSON_INPUT_LOOKS_LIKE_THAT + contentsDocument.toString());
        GatewayResponse gatewayResponse = new GatewayResponse(environment);
        try {
            if (StringUtils.isNotEmpty(contentsDocument.getIsbn())) {

                handleFiles(contentsDocument);

                logger.debug(THIS_IS_MY_CONTENTS_DOCUMENT_TO_PERSIST + contentsDocument.toString());
                try {
                    String contents = dynamoDBClient.getContents(contentsDocument.getIsbn());
                    if (StringUtils.isEmpty(contents)) {
                        dynamoDBClient.createContents(contentsDocument);
                        this.waitAMoment(HALF_A_SECOND);
                        String createdContents = dynamoDBClient.getContents(contentsDocument.getIsbn());
                        logger.info(CONTENTS_CREATED);
                        gatewayResponse.setBody(createdContents);
                        gatewayResponse.setStatusCode(HttpStatus.SC_CREATED);
                    } else {
                        String updatedContents = dynamoDBClient.updateContents(contentsDocument);
                        logger.info(CONTENTS_UPDATED);
                        gatewayResponse.setBody(updatedContents);
                        gatewayResponse.setStatusCode(HttpStatus.SC_OK);
                    }
                } catch (NotFoundException e) {
                    dynamoDBClient.createContents(contentsDocument);
                    this.waitAMoment(HALF_A_SECOND);
                    String createdContents = dynamoDBClient.getContents(contentsDocument.getIsbn());
                    logger.info(CONTENTS_CREATED);
                    gatewayResponse.setBody(createdContents);
                    gatewayResponse.setStatusCode(HttpStatus.SC_CREATED);
                } catch (Exception e) {
                    String msg = FAILED_AFTER_PERSISTING + e.getMessage();
                    logger.error(msg, e);
                    gatewayResponse.setErrorBody(msg);
                    gatewayResponse.setStatusCode(HttpStatus.SC_CONFLICT);
                }
            } else {
                logger.error(COULD_NOT_UPDATE_PROVIDED_CONTENTS + contentsDocument.toString());
                gatewayResponse.setErrorBody(COULD_NOT_UPDATE_PROVIDED_CONTENTS + contentsDocument.toString());
                gatewayResponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            }
        } catch (IOException e) {
            String msg = ERROR_STORING_FILE + e.getMessage();
            logger.error(msg, e);
            gatewayResponse.setErrorBody(msg);
            gatewayResponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
        } catch (Exception e) {
            String msg = ERROR_IN_UPDATE_FUNCTION + e.getMessage();
            logger.error(msg, e);
            gatewayResponse.setErrorBody(msg);
            gatewayResponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
        }
        return gatewayResponse;
    }

    private void handleFiles(ContentsDocument contentsDocument) throws IOException {
        if (StringUtils.isNotEmpty(contentsDocument.getImageSmall())
                && contentsDocument.getImageSmall().startsWith(HTTP_PREFIX)) {
            String objectKey = putFileS3(
                    contentsDocument.getIsbn(),
                    contentsDocument.getImageSmall(),
                    SMALL,
                    FILE_EXTENSION_JPG,
                    MIME_TYPE_IMAGE_JPG
            );
            contentsDocument.setImageSmall(objectKey);
        }

        if (StringUtils.isNotEmpty(contentsDocument.getImageLarge())
                && contentsDocument.getImageLarge().startsWith(HTTP_PREFIX)) {
            String objectKey = putFileS3(
                    contentsDocument.getIsbn(),
                    contentsDocument.getImageLarge(),
                    LARGE,
                    FILE_EXTENSION_JPG,
                    MIME_TYPE_IMAGE_JPG
            );
            contentsDocument.setImageLarge(objectKey);
        }

        if (StringUtils.isNotEmpty(contentsDocument.getImageOriginal())
                && contentsDocument.getImageOriginal().startsWith(HTTP_PREFIX)) {
            String objectKey = putFileS3(
                    contentsDocument.getIsbn(),
                    contentsDocument.getImageOriginal(),
                    ORIGINAL,
                    FILE_EXTENSION_JPG,
                    MIME_TYPE_IMAGE_JPG
            );
            contentsDocument.setImageOriginal(objectKey);
        }

        if (StringUtils.isNotEmpty(contentsDocument.getAudioFile())
                && contentsDocument.getAudioFile().startsWith(HTTP_PREFIX)) {
            String objectKey = putFileS3(
                    contentsDocument.getIsbn(),
                    contentsDocument.getAudioFile(),
                    AUDIO,
                    FILE_EXTENSION_WAV,
                    MIME_TYPE_AUDIO_WAV
            );
            contentsDocument.setAudioFile(objectKey);
        }
    }

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
            s3Client.uploadFile(
                    inputStream,
                    objectKey,
                    fileName,
                    mimeType
            );
        }

        return objectKey;
    }

    @JacocoGenerated
    private void waitAMoment(int millisec) {
        try {
            Thread.sleep(millisec);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Define the success status code.
     *
     * @param input  The request input.
     * @param output The response output
     * @return the success status code.
     */
    @Override
    protected Integer getSuccessStatusCode(ContentsRequest input, GatewayResponse output) {
        return output.getStatusCode();
    }
}
