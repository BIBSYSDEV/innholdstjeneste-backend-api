package no.unit.bibs.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class QueryContentsResponseTest {

    public static final URI EXAMPLE_CONTEXT = URI.create("https://example.org/search");
    public static final List<JsonNode> SAMPLE_HITS = Collections.EMPTY_LIST;
    public static final int SAMPLE_TOOK = 0;
    public static final int SAMPLE_TOTAL = 0;

    @Test
    void getSuccessStatusCodeReturnsOK() {
        QueryContentsResponse response =  new QueryContentsResponse(EXAMPLE_CONTEXT,
                SAMPLE_TOOK,
                SAMPLE_TOTAL,
                SAMPLE_HITS);
        assertNotNull(response);
    }


}