package no.unit.bibs.contents;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Path;

import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



public class StorageClientTest {

    public static final String CREATE_CONTENTS_EVENT = "createContentsEvent.json";
    public static final String CREATE_CONTENTS_BASE_64_EVENT = "createContentBase64EncodedImage.json";

    private StorageClient storageClient;
    private S3Connection s3Connection;


    /**
     * Set up test environment.
     **/
    @BeforeEach
    public void init() {
        s3Connection = mock(S3Connection.class);
        storageClient = new StorageClient(s3Connection);
    }

    @Test
    public void constructorWithEnvironmentDefinedShouldCreateInstance() {
        assertNotNull(storageClient);
    }

    @Test
    void testHandleFilesWithBase64EncodedImageSmall() throws IOException {
        var contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        var contentsBase64Encoded = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_BASE_64_EVENT));
        doNothing().when(s3Connection).uploadFile(any(byte[].class), anyString(), anyString(), anyString());
        var contentsDocument = dtoObjectMapper.readValue(contentsBase64Encoded, ContentsDocument.class);
        storageClient.handleFiles(contentsDocument);
        assertEquals(dtoObjectMapper.readValue(contents, ContentsDocument.class), contentsDocument);
    }

    @Test
    void testUpdateDocumentContent() throws IOException {
        var contents = IoUtils.stringFromResources(Path.of(CREATE_CONTENTS_EVENT));
        var contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);
        var mockObjectKey = "blablah";
        storageClient.updateContentDocumentWithObjectKey(contentsDocument, mockObjectKey, StorageClient.SMALL);
        assertEquals(mockObjectKey, contentsDocument.getImageSmall());
        storageClient.updateContentDocumentWithObjectKey(contentsDocument, mockObjectKey, StorageClient.LARGE);
        assertEquals(mockObjectKey, contentsDocument.getImageLarge());
        storageClient.updateContentDocumentWithObjectKey(contentsDocument, mockObjectKey, StorageClient.ORIGINAL);
        assertEquals(mockObjectKey, contentsDocument.getImageOriginal());
        storageClient.updateContentDocumentWithObjectKey(contentsDocument, mockObjectKey, StorageClient.MP3);
        assertEquals(mockObjectKey, contentsDocument.getAudioFile());
    }

}
