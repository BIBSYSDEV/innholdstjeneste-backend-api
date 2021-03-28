package no.unit.bibs.contents;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

public class S3ClientTest {

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
    
}
