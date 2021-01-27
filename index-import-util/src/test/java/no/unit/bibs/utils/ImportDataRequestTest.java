package no.unit.bibs.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImportDataRequestTest {

    private static final String SAMPLE_BUCKET = "bucket";
    private static final String SAMPLE_FOLDERKEY = "folderKey";

    @Test
    void compareImportDataRequestWithBuilderAndWithConnstructor() {
        ImportDataRequest importDataRequest1 = new ImportDataRequest(SAMPLE_BUCKET, SAMPLE_FOLDERKEY);

        ImportDataRequest importDataRequest2 = new ImportDataRequest.Builder()
                .withS3Bucket(SAMPLE_BUCKET)
                .withS3FolderKey(SAMPLE_FOLDERKEY)
                .build();

        assertEquals(importDataRequest1, importDataRequest2);
        assertEquals(importDataRequest2.getS3bucket(), SAMPLE_BUCKET);
        assertEquals(importDataRequest2.getS3folderkey(), SAMPLE_FOLDERKEY);

    }

}