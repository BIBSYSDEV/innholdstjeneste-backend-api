package no.unit.bibs.contents;

import nva.commons.utils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static nva.commons.utils.JsonUtils.objectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;



public class S3ClientTest {

    public static final String SAMPLE_PRESIGNED_S3_WRITE_URL = "https://sampleurl.com/upload?test=test";
    public static final String CREATE_CONTENTS_EVENT = "createContentsEvent.json";
    public static final String CREATE_CONTENTS_BASE_64_EVENT = "createContentBase64EncodedImage.json";

    private S3Client s3Client;
    private S3Connection s3Connection;


    /**
     * Set up test environment.
     **/
    @BeforeEach
    public void init() {
        s3Connection = mock(S3Connection.class);
        s3Client = new S3Client(s3Connection);
    }

    @Test
    public void constructorWithEnvironmentDefinedShouldCreateInstance() {
        assertNotNull(s3Client);
    }


    @Test
    void testHandleFilesWithBase64EncodedImageSmall() throws IOException {
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        String contentsBase64Encoded = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_BASE_64_EVENT));
        doNothing().when(s3Connection).uploadFile(any(), anyString(), anyString(), anyString());
        ContentsDocument contentsDocument = objectMapper.readValue(contentsBase64Encoded, ContentsDocument.class);
        s3Client.handleFiles(contentsDocument);
        assertEquals(objectMapper.readValue(contents, ContentsDocument.class), contentsDocument);
    }

    @Test
    void testUpdateDocumentContent() throws IOException {
        String contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        ContentsDocument contentsDocument = objectMapper.readValue(contents, ContentsDocument.class);
        String mockObjectKey = "blablah";
        s3Client.updateContentDocumentWithObjectKey(contentsDocument, mockObjectKey, S3Client.SMALL);

        assertEquals(mockObjectKey, contentsDocument.getImageSmall());
    }

}
