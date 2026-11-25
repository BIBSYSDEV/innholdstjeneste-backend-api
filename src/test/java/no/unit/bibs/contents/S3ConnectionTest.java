package no.unit.bibs.contents;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.MalformedURLException;
import java.net.URL;

import static no.unit.bibs.contents.S3Connection.CONTENT_DISPOSITION;
import static no.unit.bibs.contents.S3Connection.CONTENT_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class S3ConnectionTest {

    private static final String ETAG = "etag";
    private static final String SAMPLE_PRESIGNED_S3_WRITE_URL = "https://sampleurl.com/upload?test=test";
    private static final String SAMPLE_OBJECT_NAME = "testobjectname";
    private static final String SAMPLE_FILE_NAME = "testfilename";
    private static final String SAMPLE_MIME_TYPE = "testmime/type";

    private S3Presigner s3Presigner;
    private S3Client s3Client;
    private S3Connection s3Connection;

    /**
     * Set up test environment.
     **/
    @BeforeEach
    public void init() {
        s3Client = mock(S3Client.class);
        s3Presigner = mock(S3Presigner.class);
        var bucketName = "bucketname";
        s3Connection = new S3Connection(s3Client, s3Presigner, bucketName);
    }

    @Test
    void generatePresignedWriteUrl() throws MalformedURLException {
        var presignedPutObjectRequest = mock(PresignedPutObjectRequest.class);
        when(presignedPutObjectRequest.url()).thenReturn(presignedS3Url());
        when(s3Presigner.presignPutObject((PutObjectPresignRequest) any()))
                .thenReturn(presignedPutObjectRequest);
        var url = s3Connection
                .generatePresignedWriteUrl(SAMPLE_OBJECT_NAME, SAMPLE_FILE_NAME, SAMPLE_MIME_TYPE);
        assertNotNull(url);
    }

    @Test
    void shouldPutObjectInS3WithCorrectMetadataWhenUploadingFile() {
        var putResponse = mock(PutObjectResponse.class);

        var captor = ArgumentCaptor.forClass(PutObjectRequest.class);

        doReturn(ETAG).when(putResponse).eTag();
        doReturn(putResponse).when(s3Client).putObject(captor.capture(), any(RequestBody.class));

        var bytes = new byte[0];

        s3Connection.uploadFile(bytes, SAMPLE_OBJECT_NAME, SAMPLE_FILE_NAME, SAMPLE_MIME_TYPE);

        var metadata =
            captor.getAllValues()
                .stream()
                .map(PutObjectRequest::metadata)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertThat(metadata.containsKey(CONTENT_DISPOSITION), equalTo(true));
        assertThat(metadata.containsKey(CONTENT_TYPE), equalTo(true));
        assertThat(metadata.get(CONTENT_DISPOSITION), containsString(SAMPLE_FILE_NAME));
        assertThat(metadata.get(CONTENT_TYPE), containsString(SAMPLE_MIME_TYPE));

        verify(s3Client, times(1))
            .putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void shouldHandleExceptionWhenPutObjectInS3Fails() {
        var putResponse = mock(PutObjectResponse.class);

        doReturn(ETAG).when(putResponse).eTag();
        doThrow(S3Exception.class).when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        var bytes = new byte[0];

        assertThrows(S3Exception.class, () -> s3Connection.uploadFile(bytes,
                                                                      SAMPLE_OBJECT_NAME,
                                                                      SAMPLE_FILE_NAME,
                                                                      SAMPLE_MIME_TYPE));
    }

    @Test
    void shouldNotAddMetadataOnEmptyOrIncorrectMetadataFields() {
        var putResponse = mock(PutObjectResponse.class);

        var captor = ArgumentCaptor.forClass(PutObjectRequest.class);

        doReturn(ETAG).when(putResponse).eTag();
        doReturn(putResponse).when(s3Client).putObject(captor.capture(), any(RequestBody.class));

        var bytes = new byte[0];

        s3Connection.uploadFile(bytes, SAMPLE_OBJECT_NAME, null, null);
        s3Connection.uploadFile(bytes, SAMPLE_OBJECT_NAME, "", "missing-slash");

        var hasNoMetadata = captor.getAllValues()
                                .stream()
                                .allMatch(request -> request.metadata().isEmpty());

        assertThat(hasNoMetadata, equalTo(true));

        verify(s3Client, times(2))
            .putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    private URL presignedS3Url() throws MalformedURLException {
        return URI.create(SAMPLE_PRESIGNED_S3_WRITE_URL).toURL();
    }

}
