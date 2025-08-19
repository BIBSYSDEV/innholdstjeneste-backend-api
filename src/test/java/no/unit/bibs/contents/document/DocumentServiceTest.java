package no.unit.bibs.contents.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import nva.commons.apigateway.exceptions.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentServiceTest extends DocumentTestBase {

    private static final String BLANK = "";
    private static final String EXAMPLE_AUTHOR = "Test Author";
    private static final String EXAMPLE_DESCRIPTION_SHORT = "Kort beskrivelse";
    private static final String EXAMPLE_ISBN = "978-3-16-148410-0";
    private static final String EXAMPLE_SMALL_JPG = "http://example.com/small.jpg";
    private static final String EXAMPLE_SOURCE = "http://example.com/source";
    private static final String EXAMPLE_TABLE_OF_CONTENTS = "Innholdsfortegnelse";
    private static final String EXAMPLE__TITLE = "Test Title";

    @BeforeEach
    void setUp() throws JsonProcessingException, BadRequestException {
        init();
    }

    @Test
    void isValidReturnsTrueWhenImageIsIncluded() {
        var document = DocumentDao.builder()
            .isbn(EXAMPLE_ISBN)
            .source(EXAMPLE_SOURCE)
            .imageSmall(EXAMPLE_SMALL_JPG)
            .build()
            .toDto();
        assertTrue(document.isValid());
    }

    @Test
    void isValidReturnsTrueWhenDescriptionIsIncluded() {
        var document = DocumentDao.builder()
            .isbn(EXAMPLE_ISBN)
            .source(EXAMPLE_SOURCE)
            .descriptionShort(EXAMPLE_DESCRIPTION_SHORT)
            .build()
            .toDto();
        assertTrue(document.isValid());
    }

    @Test
    void isValidReturnsFalseWhenNoDescriptionOrImages() {
        var document = DocumentDao.builder()
            .isbn(EXAMPLE_ISBN)
            .source(EXAMPLE_SOURCE)
            .build()
            .toDto();
        assertFalse(document.isValid());
    }

    @Test
    void isValidReturnsTrueWhenAllFieldsAreValid() {
        var document = DocumentDao.builder()
            .isbn(EXAMPLE_ISBN)
            .source(EXAMPLE_SOURCE)
            .descriptionShort(EXAMPLE_DESCRIPTION_SHORT)
            .imageSmall(EXAMPLE_SMALL_JPG)
            .tableOfContents(EXAMPLE_TABLE_OF_CONTENTS)
            .build()
            .toDto();
        assertTrue(document.isValid());
    }

    @Test
    void escapedStringIsHandledAndIsValidReturnsTrue() {
        var document = DocumentDao.builder()
            .isbn(EXAMPLE_ISBN)
            .source(EXAMPLE_SOURCE)
            .descriptionShort("Tro, h&aring;p &amp; kj&oslash;rlighet")
            .descriptionLong("Tro, h&aring;p &amp; kj&oslash;rlighet")
            .imageSmall(EXAMPLE_SMALL_JPG)
            .title("Dette er en &AElig;&Oslash;&Aring; test")
            .author("&AElig;sops fabler")
            .promotional("Tro, h&aring;p &amp; kj&oslash;rlighet")
            .summary("Tro, h&aring;p &amp; kj&oslash;rlighet")
            .review("Tro, h&aring;p &amp; kj&oslash;rlighet")
            .tableOfContents("Tro, h&aring;p &amp; kj&oslash;rlighet")
            .build()
            .toDto();
        assertTrue(document.isValid());
    }

    @Test
    void validateDocumentThrowsExceptionWhenIsbnIsBlank() {
        var document = DocumentDao.builder()
            .isbn(BLANK)
            .title(EXAMPLE__TITLE)
            .author(EXAMPLE_AUTHOR)
            .build()
            .toDto();
        assertThrows(BadRequestException.class, () -> documentService.validateDocument(document));
    }

    @Test
    void validateDocumentThrowsExceptionWhenTitleIsBlank() {
        var document = DocumentDao.builder()
            .isbn(EXAMPLE_ISBN)
            .title(BLANK)
            .author(EXAMPLE_AUTHOR)
            .build()
            .toDto();
        assertThrows(BadRequestException.class, () -> documentService.validateDocument(document));
    }

    @Test
    void validateDocumentThrowsExceptionWhenAuthorIsBlank() {
        var document = DocumentDao.builder()
            .isbn(EXAMPLE_ISBN)
            .title(EXAMPLE__TITLE)
            .author(BLANK)
            .build()
            .toDto();
        assertThrows(BadRequestException.class, () -> documentService.validateDocument(document));
    }
}