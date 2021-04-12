package no.unit.bibs.contents;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class S3ClientTest {

    public static final String SAMPLE_PRESIGNED_S3_WRITE_URL = "https://sampleurl.com/upload?test=test";
    private static final String SAMPLE_OBJECT_NAME = "testobjectname";
    private static final String SAMPLE_FILE_NAME = "testfilename";
    private static final String SAMPLE_MIME_TYPE = "testmime/type";

    private S3Client s3Client;

    private AmazonS3 amazonS3Client;
    private String bucketName;


    /**
     * Set up test environment.
     **/
    @BeforeEach
    public void init() {
        amazonS3Client  = mock(AmazonS3.class);
        s3Client = new S3Client(amazonS3Client, bucketName);
    }

    @Test
    public void constructorWithEnvironmentDefinedShouldCreateInstance() {
        S3Client s3Client = new S3Client(amazonS3Client, bucketName);
        assertNotNull(s3Client);
    }

    @Test
    void generatePresignedWriteUrl() throws MalformedURLException {
        S3Client s3Client = new S3Client(amazonS3Client, bucketName);
        when(amazonS3Client.generatePresignedUrl(any()))
                .thenReturn(new URL(SAMPLE_PRESIGNED_S3_WRITE_URL));
        URL url = s3Client
                .generatePresignedWriteUrl(SAMPLE_OBJECT_NAME, SAMPLE_FILE_NAME, SAMPLE_MIME_TYPE);
        assertNotNull(url);
    }

}
