package no.unit.bibs.contents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class S3ConnectionTest {

    S3Presigner s3Presigner;
    S3Connection s3Connection;
    private String bucketName = "bucketname";

    public static final String SAMPLE_PRESIGNED_S3_WRITE_URL = "https://sampleurl.com/upload?test=test";
    private static final String SAMPLE_OBJECT_NAME = "testobjectname";
    private static final String SAMPLE_FILE_NAME = "testfilename";
    private static final String SAMPLE_MIME_TYPE = "testmime/type";

    /**
     * Set up test environment.
     **/
    @BeforeEach
    public void init() {
        s3Presigner = mock(S3Presigner.class);
        HttpURLConnection httpUrlConnection = mock(HttpURLConnection.class);
        s3Connection = new S3Connection(s3Presigner, bucketName, httpUrlConnection);
    }

    @Test
    void generatePresignedWriteUrl() throws MalformedURLException {

        PresignedPutObjectRequest presignedPutObjectRequest = mock(PresignedPutObjectRequest.class);
        when(presignedPutObjectRequest.url()).thenReturn(new URL(SAMPLE_PRESIGNED_S3_WRITE_URL));

        when(s3Presigner.presignPutObject((PutObjectPresignRequest) any()))
                .thenReturn(presignedPutObjectRequest);

        URL url = s3Connection
                .generatePresignedWriteUrl(SAMPLE_OBJECT_NAME, SAMPLE_FILE_NAME, SAMPLE_MIME_TYPE);
        assertNotNull(url);
    }
}
