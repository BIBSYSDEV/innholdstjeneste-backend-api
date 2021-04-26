package no.unit.bibs.contents;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContentsRequestTest {

    public static final String STRING = "string";
    public static final Instant INSTANT = Instant.now();

    @Test
    public void testBuilderConstructor() {
        ContentsRequest.Builder builder = new ContentsRequest.Builder();
        builder.withContents(new ContentsDocument(STRING, STRING, STRING, STRING, STRING, STRING, STRING, STRING,
                STRING, STRING, STRING, STRING, STRING, STRING, STRING, INSTANT, INSTANT));
        ContentsRequest request = new ContentsRequest(builder);
        assertEquals(STRING, request.getContents().getTitle());
    }

}
