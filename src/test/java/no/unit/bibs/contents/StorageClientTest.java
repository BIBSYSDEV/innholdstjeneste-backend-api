package no.unit.bibs.contents;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.file.Path;

import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StorageClientTest {

    public static final String CREATE_CONTENTS_EVENT = "createContentsEvent.json";
    public static final String CREATE_CONTENTS_EVENT_ALL_VALUES = "createContentsEventForS3.json";
    public static final String CREATE_CONTENTS_BASE_64_EVENT = "createContentBase64EncodedImage.json";
    public static final String CREATE_CONTENTS_EVENT_SINGLE_FILE_VALUE = "createContentsEventSingleS3File.json";

    private StorageClient storageClient;
    private S3Connection s3Connection;
    @SuppressWarnings("all")
    private HttpResponse httpResponse;
    private HttpClient httpClient;


    /**
     * Set up test environment.
     **/
    @BeforeEach
    public void init() {
        s3Connection = mock(S3Connection.class);
        httpClient = mock(HttpClient.class);
        httpResponse = mock(HttpResponse.class);
        doNothing().when(s3Connection).uploadFile(any(), any(), any(), any());

        storageClient = new StorageClient(s3Connection,  httpClient);
    }

    @Test
    void testHandleFilesWithBase64EncodedImageSmall() throws IOException {
        var contents = getContents(CREATE_CONTENTS_EVENT);
        var contentsBase64Encoded = getContents(CREATE_CONTENTS_BASE_64_EVENT);
        var contentsDocument = dtoObjectMapper.readValue(contentsBase64Encoded, ContentsDocument.class);
        storageClient.handleFiles(contentsDocument);
        assertEquals(dtoObjectMapper.readValue(contents, ContentsDocument.class), contentsDocument);
    }

    @Test
    void testUpdateDocumentContent() throws IOException {
        var contents = getContents(CREATE_CONTENTS_EVENT);
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
        storageClient.updateContentDocumentWithObjectKey(contentsDocument, mockObjectKey, "SomethingElse");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldHandleDifferentTypeOfContentGettingCorrectS3KeyWhenUploadingToS3()
        throws IOException, InterruptedException {

        doReturn(200).when(httpResponse).statusCode();

        doReturn(httpResponse).when(httpClient).send(any(), any(HttpResponse.BodyHandlers.discarding().getClass()));
        doReturn(createMockHttpByteResponse(200))
            .when(httpClient)
            .send(any(), any(HttpResponse.BodyHandlers.ofByteArray().getClass()));

        storageClient = new StorageClient(s3Connection,  httpClient);

        var contents = getContents(CREATE_CONTENTS_EVENT_ALL_VALUES);
        var contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);

        storageClient.handleFiles(contentsDocument);

        assertThat(contentsDocument.getImageSmall(), equalTo("files/images/small/7/4/9788205377547.jpg"));
        assertThat(contentsDocument.getImageLarge(), equalTo("files/images/large/7/4/9788205377547.jpg"));
        assertThat(contentsDocument.getImageOriginal(), equalTo("files/images/original/7/4/9788205377547.jpg"));
        assertThat(contentsDocument.getAudioFile(), equalTo("files/audio/mp3/7/4/9788205377547.mp3"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotProcessFileThatIsNotDownloadable() throws Exception {

        doReturn(200).when(httpResponse).statusCode();
        doReturn(httpResponse).when(httpClient).send(any(), any(HttpResponse.BodyHandlers.discarding().getClass()));

        doReturn(createMockHttpByteResponse(200))
            .when(httpClient)
            .send(any(), any(HttpResponse.BodyHandlers.ofByteArray().getClass()));

        storageClient = new StorageClient(s3Connection,  httpClient);

        var contents = getContents(CREATE_CONTENTS_EVENT_ALL_VALUES)
                           .replace("https://www.example.com/image-large.jpg",
                                    "https://....www.example.com/image-large.jpg");
        var contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);

        storageClient.handleFiles(contentsDocument);

        assertThat(contentsDocument.getImageLarge(), equalTo(null)); // No longer there after processing
        assertThat(contentsDocument.getImageSmall(), equalTo("files/images/small/7/4/9788205377547.jpg")); // Still here
    }

    @Test
    void shouldNotUploadToS3WhenNonSuccessfulFetchHttpResource()
        throws IOException, InterruptedException {

        doReturn(200).when(httpResponse).statusCode();
        doReturn(httpResponse).when(httpClient).send(any(), any(HttpResponse.BodyHandlers.discarding().getClass()));

        doReturn(createMockHttpByteResponse(500))
            .when(httpClient)
            .send(any(), any(HttpResponse.BodyHandlers.ofByteArray().getClass()));

        storageClient = new StorageClient(s3Connection,  httpClient);

        var contents = getContents(CREATE_CONTENTS_EVENT_SINGLE_FILE_VALUE)
                           .replace("https://....www.example.com/image-original.jpg",
                                    "https://www.example.com/image-original.jpg");
        var contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);

        storageClient.handleFiles(contentsDocument);

        verify(s3Connection, times(0)).uploadFile(any(), any(), any(), any());
    }

    @Test
    void shouldNotUploadToS3WhenNonSuccessfulHttpResponseOnDownloadableFileCheck()
        throws IOException, InterruptedException {

        doReturn(500).when(httpResponse).statusCode();
        doReturn(httpResponse).when(httpClient).send(any(), any(HttpResponse.BodyHandlers.discarding().getClass()));

        storageClient = new StorageClient(s3Connection,  httpClient);

        var contents = getContents(CREATE_CONTENTS_EVENT_SINGLE_FILE_VALUE)
                           .replace("https://....www.example.com/image-original.jpg",
                                    "https://www.example.com/image-original.jpg");
        var contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);

        storageClient.handleFiles(contentsDocument);

        verify(s3Connection, times(0)).uploadFile(any(), any(), any(), any());
    }

    @Test
    void shouldNotUploadToS3FilesThatAreNotHttpSchemeLinks() throws IOException {
        var contents = getContents(CREATE_CONTENTS_EVENT_SINGLE_FILE_VALUE).replace("https", "ftp");
        var contentsDocument = dtoObjectMapper.readValue(contents, ContentsDocument.class);

        storageClient.handleFiles(contentsDocument);

        verify(s3Connection, times(0)).uploadFile(any(), any(), any(), any());
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<byte[]> createMockHttpByteResponse(int statusCode) {
        var byteResponse = mock(HttpResponse.class);
        doReturn(statusCode).when(byteResponse).statusCode();
        doReturn(new byte[0]).when(httpResponse).body();

        return byteResponse;
    }

    private String getContents(String path) {
        return IoUtils.stringFromResources(Path.of(path));
    }
}
