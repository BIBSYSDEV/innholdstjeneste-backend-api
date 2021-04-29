package no.unit.bibs.contents;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringHelperTest {

    @Test
    public void testUnescaping() {
        String input = "&Oslash;konomi er g&oslash;y.";
        assertTrue(StringHelper.isValidHtmlEscapeCode(input));

        input = "Å være utvikler er gøy.";
        assertFalse(StringHelper.isValidHtmlEscapeCode(input));

    }

}