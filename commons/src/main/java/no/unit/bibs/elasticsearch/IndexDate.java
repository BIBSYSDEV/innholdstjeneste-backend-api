package no.unit.bibs.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import nva.commons.utils.JacocoGenerated;

import java.util.Objects;

import static java.util.Objects.nonNull;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class IndexDate {

    public static final String YEAR_JSON_POINTER = "/entityDescription/m/date/m/year/s";
    public static final String MONTH_JSON_POINTER = "/entityDescription/m/date/m/month/s";
    public static final String DAY_JSON_POINTER = "/entityDescription/m/date/m/day/s";

    private final String year;
    private final String month;
    private final String day;

    /**
     * Constructor for basic deserialization of IndexDate.
     * @param year String representing year.
     * @param month String representing month.
     * @param day String representing day.
     */
    @JsonCreator
    public IndexDate(@JsonProperty("year") String year,
                     @JsonProperty("month") String month,
                     @JsonProperty("day") String day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    /**
     * Constructor for IndexDate from a DynamoDb generated StreamRecord.
     * @param streamRecord JsonNode representation of a DynamoDb StreamRecord.
     */
    public IndexDate(JsonNode streamRecord) {
        this.year = extractYear(streamRecord);
        this.month = extractMonth(streamRecord);
        this.day = extractDay(streamRecord);
    }

    public String getYear() {
        return year;
    }

    public String getMonth() {
        return month;
    }

    public String getDay() {
        return day;
    }

    @JacocoGenerated
    @JsonIgnore
    public boolean isPopulated() {
        return isNotNullOrEmpty(year) || isNotNullOrEmpty(month) || isNotNullOrEmpty(day);
    }

    private String extractDay(JsonNode record) {
        return textFromNode(record, DAY_JSON_POINTER);
    }

    @JacocoGenerated
    private boolean isNotNullOrEmpty(String string) {
        return nonNull(string) && !string.isEmpty();
    }

    private String extractMonth(JsonNode record) {
        return textFromNode(record, MONTH_JSON_POINTER);
    }

    private String extractYear(JsonNode record) {
        return textFromNode(record, YEAR_JSON_POINTER);
    }

    private String textFromNode(JsonNode jsonNode, String jsonPointer) {
        JsonNode json = jsonNode.at(jsonPointer);
        return isPopulatedJsonPointer(json) ? json.asText() : null;
    }

    @JacocoGenerated
    private boolean isPopulatedJsonPointer(JsonNode json) {
        return !json.isNull() && !json.asText().isBlank();
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IndexDate)) {
            return false;
        }
        IndexDate date = (IndexDate) o;
        return Objects.equals(getYear(), date.getYear())
                && Objects.equals(getMonth(), date.getMonth())
                && Objects.equals(getDay(), date.getDay());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getYear(), getMonth(), getDay());
    }
}

