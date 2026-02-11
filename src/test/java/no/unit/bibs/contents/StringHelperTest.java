package no.unit.bibs.contents;

import org.junit.jupiter.api.Test;

import static no.unit.bibs.contents.StringHelper.isValidHtmlEscapeCode;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringHelperTest {

    @Test
    void shouldReturnFalseForNull() {
        assertFalse(isValidHtmlEscapeCode(null));
    }

    @Test
    void shouldReturnFalseWhenNoEntityFound() {
        assertFalse(isValidHtmlEscapeCode("Å være utvikler er gøy."));
    }

    // Hexadecimal entities (group 1)
    @Test
    void shouldReturnTrueForValidHexEntity() {
        assertTrue(isValidHtmlEscapeCode("&#x41;"));
    }

    @Test
    void shouldReturnFalseForOversizedHexEntity() {
        assertFalse(isValidHtmlEscapeCode("&#x1234567;"));
    }

    @Test
    void shouldReturnFalseForInvalidHexNumberFormat() {
        assertFalse(isValidHtmlEscapeCode("&#xGGGG;"));
    }

    @Test
    void shouldReturnFalseForHexInSurrogateRange() {
        assertFalse(isValidHtmlEscapeCode("&#xD800;"));
    }

    @Test
    void shouldReturnFalseForHexAboveMaxCodePoint() {
        assertFalse(isValidHtmlEscapeCode("&#x110000;"));
    }

    // Decimal entities (group 2)
    @Test
    void shouldReturnTrueForValidDecimalEntity() {
        assertTrue(isValidHtmlEscapeCode("&#65;"));
    }

    @Test
    void shouldReturnFalseForOversizedDecimalEntity() {
        assertFalse(isValidHtmlEscapeCode("&#12345678;"));
    }

    @Test
    void shouldReturnFalseForInvalidDecimalNumberFormat() {
        assertFalse(isValidHtmlEscapeCode("&#ABC;"));
    }

    @Test
    void shouldReturnFalseForDecimalInSurrogateRange() {
        assertFalse(isValidHtmlEscapeCode("&#55296;"));
    }

    @Test
    void shouldReturnFalseForDecimalAboveMaxCodePoint() {
        assertFalse(isValidHtmlEscapeCode("&#1114112;"));
    }

    // Named entities (group 3)
    @Test
    void shouldReturnTrueForValidNamedEntity() {
        assertTrue(isValidHtmlEscapeCode("&Oslash;"));
    }

    @Test
    void shouldReturnFalseForInvalidNamedEntity() {
        assertFalse(isValidHtmlEscapeCode("&invalidname;"));
    }

}