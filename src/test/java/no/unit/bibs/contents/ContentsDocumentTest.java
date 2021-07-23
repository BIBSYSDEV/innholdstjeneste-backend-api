package no.unit.bibs.contents;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentsDocumentTest {

    @Test
    void isValidWithValidContentsDocument() {
        ContentsDocument contentsDocument = new ContentsDocument("title", "", "", "isbn", "desc_short", "",
                "", "", " ", "", "", "", "/image/org",
                "", "SOURCE", Instant.now(), Instant.now());
        assertTrue(contentsDocument.isValid());
    }

    @Test
    void isValidWithNullIsbnOnContentsDocument() {
        ContentsDocument contentsDocument = new ContentsDocument("title", "", "", null, "desc_short", "",
                "", "", " ", "", "", "", "/image/org",
                "", "SOURCE", Instant.now(), Instant.now());
        assertFalse(contentsDocument.isValid());
    }

    @Test
    void isValidWithBlankTocOnContentsDocument() {
        ContentsDocument contentsDocument = new ContentsDocument("title", "", "", "isbn", "", "",
                " ", "", " ", "", "", "", "",
                "", "SOURCE", Instant.now(), Instant.now());
        assertFalse(contentsDocument.isValid());
    }

    @Test
    void isValidWithoutSourceOnContentsDocument() {
        ContentsDocument contentsDocument = new ContentsDocument("title", "", "", "isbn", "", "",
                "toc", "", " ", "", "", "", "",
                "", "", Instant.now(), Instant.now());
        assertFalse(contentsDocument.isValid());
    }

    @Test
    void isValidWithOnlyImageOnContentsDocument() {
        ContentsDocument contentsDocument = new ContentsDocument("title", "", "", "isbn", "", "",
                "", "", " ", "", "", "imgLarge", "",
                "", "SOURCE", Instant.now(), Instant.now());
        assertTrue(contentsDocument.isValid());
    }
}