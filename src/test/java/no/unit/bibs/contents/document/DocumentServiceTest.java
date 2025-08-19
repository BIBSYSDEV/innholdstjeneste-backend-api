package no.unit.bibs.contents.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import nva.commons.apigateway.exceptions.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentServiceTest extends DocumentTestBase {

    @BeforeEach
    void setUp() throws JsonProcessingException, BadRequestException {
        init();
    }

    @Test
    void isValidReturnsTrueWhenImageIsIncluded() {
        var document = DocumentDao.builder()
            .isbn("978-3-16-148410-0")
            .source("http://example.com/source")
            .imageSmall("http://example.com/small.jpg")
            .build()
            .toDto();
        assertTrue(document.isValid());
    }

    @Test
    void isValidReturnsTrueWhenDescriptionIsIncluded() {
        var document = DocumentDao.builder()
            .isbn("978-3-16-148410-0")
            .source("http://example.com/source")
            .descriptionShort("Kort beskrivelse")
            .build()
            .toDto();
        assertTrue(document.isValid());
    }

    @Test
    void isValidReturnsFalseWhenNoDescriptionOrImages() {
        var document = DocumentDao.builder()
            .isbn("978-3-16-148410-0")
            .source("http://example.com/source")
            .build()
            .toDto();
        assertFalse(document.isValid());
    }

    @Test
    void isValidReturnsTrueWhenAllFieldsAreValid() {
        var document = DocumentDao.builder()
            .isbn("978-3-16-148410-0")
            .source("http://example.com/source")
            .descriptionShort("Kort beskrivelse")
            .imageSmall("http://example.com/small.jpg")
            .tableOfContents("Innholdsfortegnelse")
            .build()
            .toDto();
        assertTrue(document.isValid());
    }

    @Test
    void escapedStringIsHandledAndIsValidReturnsTrue() {
        var document = DocumentDao.builder()
            .isbn("978-3-16-148410-0")
            .source("http://example.com/source")
            .descriptionShort("Tro, h&aring;p &amp; kj&oslash;rlighet")
            .descriptionLong("Tro, h&aring;p &amp; kj&oslash;rlighet")
            .imageSmall("http://example.com/small.jpg")
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
            .isbn("")
            .title("Test Title")
            .author("Test Author")
            .build()
            .toDto();
        assertThrows(BadRequestException.class, () -> documentService.validateDocument(document));
    }

    @Test
    void validateDocumentThrowsExceptionWhenTitleIsBlank() {
        var document = DocumentDao.builder()
            .isbn("978-3-16-148410-0")
            .title("")
            .author("Test Author")
            .build()
            .toDto();
        assertThrows(BadRequestException.class, () -> documentService.validateDocument(document));
    }

    @Test
    void validateDocumentThrowsExceptionWhenAuthorIsBlank() {
        var document = DocumentDao.builder()
            .isbn("978-3-16-148410-0")
            .title("Test Title")
            .author("")
            .build()
            .toDto();
        assertThrows(BadRequestException.class, () -> documentService.validateDocument(document));
    }
}