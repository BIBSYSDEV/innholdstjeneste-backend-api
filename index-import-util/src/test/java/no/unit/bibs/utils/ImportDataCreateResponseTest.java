package no.unit.bibs.utils;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImportDataCreateResponseTest {

    private static final String SAMPLE_MESSAGE  = "message";
    private static final String SAMPLE_BUCKET = "bucket";
    private static final String SAMPLE_FOLDERKEY = "folderKey";
    private static  final ImportDataRequest SAMPLE_REQUEST = new ImportDataRequest(SAMPLE_BUCKET, SAMPLE_FOLDERKEY);
    private static  final Integer SAMPLE_STATUS_CODE = HttpStatus.SC_OK;
    private static  final Instant SAMPLE_TIMESTAMP = Instant.now();

    @Test
    void compareImportDataCreateResponseWithBuilderAndWithConnstructor() {
        ImportDataCreateResponse importDataCreateResponse =
                new ImportDataCreateResponse(SAMPLE_MESSAGE, SAMPLE_REQUEST,  SAMPLE_STATUS_CODE, SAMPLE_TIMESTAMP);

        assertEquals(importDataCreateResponse.getMessage(), SAMPLE_MESSAGE);
        assertEquals(importDataCreateResponse.getRequest(), SAMPLE_REQUEST);
        assertEquals(importDataCreateResponse.getStatusCode(), SAMPLE_STATUS_CODE);
        assertEquals(importDataCreateResponse.getTimestamp(), SAMPLE_TIMESTAMP);


    }


}