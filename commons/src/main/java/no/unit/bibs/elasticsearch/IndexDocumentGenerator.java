package no.unit.bibs.elasticsearch;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static nva.commons.utils.StringUtils.isEmpty;

@SuppressWarnings("PMD.GodClass")
public final class IndexDocumentGenerator extends IndexDocument {

    public static final String CONTRIBUTOR_LIST_JSON_POINTER = "/contents/contributors";
    public static final String ISBN_LIST_JSON_POINTER = "/contents/isbn";
    public static final String TITLE_JSON_POINTER = "/contents/title";
    public static final String YEAR_JSON_POINTER = "/contents/year";
    public static final String IMAGE_URL_SMALL_JSON_POINTER = "/contents/image_url_small";
    public static final String IMAGE_URL_LARGE_JSON_POINTER = "/contents/image_url_large";
    public static final String IMAGE_URL_ORIGINAL_JSON_POINTER = "/contents/image_url_original";
    public static final String DESCRIPTION_SHORT_JSON_POINTER = "/contents/description_short";
    public static final String DESCRIPTION_LONG_JSON_POINTER = "/contents/description_long";
    public static final String TABLE_OF_CONTENTS_JSON_POINTER = "/contents/table_of_contents";
    public static final String MODIFIED_DATE_JSON_POINTER = "/contents/modifiedDate";
    public static final String CREATED_DATE_JSON_POINTER = "/contents/createdDate";
    public static final String SOURCE_JSON_POINTER = "/contents/source";

    public static final String MISSING_FIELD_LOGGER_WARNING_TEMPLATE =
            "The data were incomplete, missing field {} on id: {}";
    public static final String DATE_FIELD_FORMAT_ERROR_LOGGER_WARNING_TEMPLATE =
            "The data was incorrect, field {} on id: {}, ignoring value {}";
    public static final String EXCEPTION_READING_IMAGE_URL_MESSAGE = "Exception reading image url, recordId={}";

    public static final String STATUS = "status";
    public static final String TITLE = "title";
    public static final String DESCRIPTION_SHORT = "descriptionShort";
    public static final String DESCRIPTION_LONG = "descriptionLong";
    public static final String TABLE_OF_CONTENTS = "tableOfContents";
    public static final String MODIFIED_DATE = "modifiedDate";
    public static final String CREATED_DATE = "createdDate";
    public static final String YEAR = "year";
    public static final String SOURCE = "source";

    private static final ObjectMapper mapper = JsonUtils.objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(IndexDocumentGenerator.class);

    @JacocoGenerated
    private IndexDocumentGenerator(IndexDocumentBuilder builder) {
        super(builder);
    }

    /**
     * Initialise an IndexDocumentGenerator from an jsonNode.
     * @param record jsonNode containing publication data to be indexed
     * @return a generator ready to make indexDocuments
     */
    public static IndexDocumentGenerator fromJsonNode(JsonNode record) {
        String isbn = extractIsbnList(record).get(0);

        IndexDocumentBuilder builder = new IndexDocumentBuilder()
                .withIsbn(extractIsbnList(record))
                .withTitle(extractTitle(record, isbn))
                .withContributors(extractContributors(record))
                .withYear(extractYear(record, isbn))
                .withIsbn(extractIsbnList(record))
                .withShortDescription(extractShortDescription(record, isbn))
                .withLongDescription(extractLongDescription(record, isbn))
                .withTableOfContents(extractTableOfContents(record, isbn))
                .withSource(extractSource(record, isbn))
                .withModifiedDate(extractModifiedDate(record, isbn))
                .withCreatedDate(extractCreatedDate(record, isbn));

        Optional<URI> optionalURIsmall = extractImageUrlSmall(record, isbn);
        optionalURIsmall.ifPresent(builder::withSmallImage);
        Optional<URI> optionalURIlarge = extractImageUrlLarge(record, isbn);
        optionalURIlarge.ifPresent(builder::withLargeImage);
        Optional<URI> optionalURIoriginal = extractImageUrlOrginal(record, isbn);
        optionalURIoriginal.ifPresent(builder::withOriginalImage);

        return new IndexDocumentGenerator(builder);
    }

    private static List<String> extractContributors(JsonNode record) {
        return toStream(record.at(CONTRIBUTOR_LIST_JSON_POINTER))
                .map(IndexDocumentGenerator::extractAsText)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static List<String> extractIsbnList(JsonNode record) {
        return toStream(record.at(ISBN_LIST_JSON_POINTER))
                .map(IndexDocumentGenerator::extractAsText)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static String extractAsText(JsonNode jsonNode) {
        String name = jsonNode.asText();
        return nonNull(name) ? name : null;
    }

    private static String extractTitle(JsonNode record, String id) {
        var title = textFromNode(record, TITLE_JSON_POINTER);
        if (isNull(title)) {
            logMissingField(id, TITLE);
        }
        return title;
    }

    private static Instant extractYear(JsonNode record, String id) {
        return getInstant(record, id, YEAR_JSON_POINTER, YEAR);
    }

    @JacocoGenerated
    private static Optional<URI> extractImageUrlSmall(JsonNode record, String id) {
        String textFromNode = textFromNode(record, IMAGE_URL_SMALL_JSON_POINTER);
        try {
            if (!isEmpty(textFromNode)) {
                return Optional.of(getUri(textFromNode));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.warn(EXCEPTION_READING_IMAGE_URL_MESSAGE, id);
            return Optional.empty();
        }
    }

    @JacocoGenerated
    private static Optional<URI> extractImageUrlLarge(JsonNode record, String id) {
        String textFromNode = textFromNode(record, IMAGE_URL_LARGE_JSON_POINTER);
        try {
            if (!isEmpty(textFromNode)) {
                return Optional.of(getUri(textFromNode));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.warn(EXCEPTION_READING_IMAGE_URL_MESSAGE, id);
            return Optional.empty();
        }
    }

    @JacocoGenerated
    private static Optional<URI> extractImageUrlOrginal(JsonNode record, String id) {
        String textFromNode = textFromNode(record, IMAGE_URL_ORIGINAL_JSON_POINTER);
        try {
            if (!isEmpty(textFromNode)) {
                return Optional.of(getUri(textFromNode));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.warn(EXCEPTION_READING_IMAGE_URL_MESSAGE, id);
            return Optional.empty();
        }
    }

    private static URI getUri(String textFromNode) throws URISyntaxException {
        return new URI(textFromNode);
    }

    private static String extractShortDescription(JsonNode record, String id) {
        var description = textFromNode(record, DESCRIPTION_SHORT_JSON_POINTER);
        if (isNull(description)) {
            logMissingField(id, DESCRIPTION_SHORT);
        }
        return description;
    }

    private static String extractLongDescription(JsonNode record, String id) {
        var description = textFromNode(record, DESCRIPTION_LONG_JSON_POINTER);
        if (isNull(description)) {
            logMissingField(id, DESCRIPTION_LONG);
        }
        return description;
    }

    private static String extractTableOfContents(JsonNode record, String id) {
        var publicationAbstract = textFromNode(record, TABLE_OF_CONTENTS_JSON_POINTER);
        if (isNull(publicationAbstract)) {
            logMissingField(id, TABLE_OF_CONTENTS);
        }
        return publicationAbstract;
    }

    private static String extractSource(JsonNode record, String id) {
        var publicationAbstract = textFromNode(record, SOURCE_JSON_POINTER);
        if (isNull(publicationAbstract)) {
            logMissingField(id, SOURCE);
        }
        return publicationAbstract;
    }

    private static Instant extractModifiedDate(JsonNode record, String id) {
        return getInstant(record, id, MODIFIED_DATE_JSON_POINTER, MODIFIED_DATE);
    }

    private static Instant extractCreatedDate(JsonNode record, String id) {
        return getInstant(record, id, CREATED_DATE_JSON_POINTER, CREATED_DATE);
    }

    @JacocoGenerated
    private static Instant getInstant(JsonNode record, String id, String fieldJsonPtr, String fieldName) {
        String textFromNode = textFromNode(record, fieldJsonPtr);
        if (isEmpty(textFromNode)) {
            logMissingField(id, fieldName);
            return null;
        } else {
            Instant instant = null;
            try {
                instant = Instant.parse(textFromNode);
            } catch (DateTimeParseException ignored) {
                logger.warn(DATE_FIELD_FORMAT_ERROR_LOGGER_WARNING_TEMPLATE, textFromNode, id, fieldName);
            }
            if (isNull(instant)) {
                logMissingField(id, fieldName);
            }
            return instant;
        }
    }

    private static void logMissingField(String id, String field) {
        logger.warn(MISSING_FIELD_LOGGER_WARNING_TEMPLATE, field, id);
    }

    private static String textFromNode(JsonNode jsonNode, String jsonPointer) {
        JsonNode json = jsonNode.at(jsonPointer);
        return isPopulated(json) ? json.asText() : null;
    }

    private static boolean isPopulated(JsonNode json) {
        return !json.isNull() && !json.asText().isBlank();
    }

    private static JsonNode toJsonNode(DynamodbEvent.DynamodbStreamRecord streamRecord) {
        return mapper.valueToTree(streamRecord.getDynamodb().getNewImage());
    }

    private static Stream<JsonNode> toStream(JsonNode contributors) {
        return StreamSupport.stream(contributors.spliterator(), false);
    }

    public IndexDocument toIndexDocument() {
        return this;
    }
}
