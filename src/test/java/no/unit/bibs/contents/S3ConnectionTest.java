package no.unit.bibs.contents;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class S3ConnectionTest {

    AmazonS3 amazonS3Client;
    S3Connection s3Connection;
    private String bucketName;

    public static final String SAMPLE_PRESIGNED_S3_WRITE_URL = "https://sampleurl.com/upload?test=test";
    private static final String SAMPLE_OBJECT_NAME = "testobjectname";
    private static final String SAMPLE_FILE_NAME = "testfilename";
    private static final String SAMPLE_MIME_TYPE = "testmime/type";

    /**
     * Set up test environment.
     **/
    @BeforeEach
    public void init() {
        amazonS3Client = mock(AmazonS3.class);
        HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
        s3Connection = new S3Connection(amazonS3Client, bucketName, httpURLConnection);
    }

    @Test
    void generatePresignedWriteUrl() throws MalformedURLException {
        when(amazonS3Client.generatePresignedUrl(any()))
                .thenReturn(new URL(SAMPLE_PRESIGNED_S3_WRITE_URL));
        URL url = s3Connection
                .generatePresignedWriteUrl(SAMPLE_OBJECT_NAME, SAMPLE_FILE_NAME, SAMPLE_MIME_TYPE);
        assertNotNull(url);
    }
}
