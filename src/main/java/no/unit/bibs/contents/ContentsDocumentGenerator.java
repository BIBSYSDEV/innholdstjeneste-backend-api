package no.unit.bibs.contents;

import com.fasterxml.jackson.databind.JsonNode;
import nva.commons.utils.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import static java.util.Objects.isNull;
import static nva.commons.utils.StringUtils.isEmpty;

@SuppressWarnings("PMD.GodClass")
public final class ContentsDocumentGenerator extends ContentsDocument {

    public static final String ISBN_JSON_POINTER = "/isbn";
    public static final String TITLE_JSON_POINTER = "/title";
    public static final String AUTHOR_JSON_POINTER = "/author";
    public static final String YEAR_JSON_POINTER = "/year";
    public static final String IMAGE_URL_SMALL_JSON_POINTER = "/image_url_small";
    public static final String IMAGE_URL_LARGE_JSON_POINTER = "/image_url_large";
    public static final String IMAGE_URL_ORIGINAL_JSON_POINTER = "/image_url_original";
    public static final String DESCRIPTION_SHORT_JSON_POINTER = "/description_short";
    public static final String DESCRIPTION_LONG_JSON_POINTER = "/description_long";
    public static final String TABLE_OF_CONTENTS_JSON_POINTER = "/table_of_contents";
    public static final String MODIFIED_DATE_JSON_POINTER = "/modifiedDate";
    public static final String CREATED_DATE_JSON_POINTER = "/createdDate";
    public static final String SOURCE_JSON_POINTER = "/source";

    public static final String MISSING_FIELD_LOGGER_WARNING_TEMPLATE =
            "The data were incomplete, missing field {} on id: {}";
    public static final String DATE_FIELD_FORMAT_ERROR_LOGGER_WARNING_TEMPLATE =
            "The data was incorrect, field {} on id: {}, ignoring value {}";
    public static final String TITLE = "title";
    public static final String AUTHOR = "author";
    public static final String ISBN = "isbn";
    public static final String DESCRIPTION_SHORT = "descriptionShort";
    public static final String DESCRIPTION_LONG = "descriptionLong";
    public static final String TABLE_OF_CONTENTS = "tableOfContents";
    public static final String IMAGE_URL_SMALL = "image_url_small";
    public static final String IMAGE_URL_LARGE = "image_url_large";
    public static final String IMAGE_URL_ORIGINAL = "image_url_original";
    public static final String MODIFIED_DATE = "modifiedDate";
    public static final String CREATED_DATE = "createdDate";
    public static final String YEAR = "year";
    public static final String SOURCE = "source";

    private static final Logger logger = LoggerFactory.getLogger(ContentsDocumentGenerator.class);

    @JacocoGenerated
    private ContentsDocumentGenerator(ContentsDocumentBuilder builder) {
        super(builder);
    }

    /**
     * Initialise an IndexDocumentGenerator from an jsonNode.
     * @param record jsonNode containing publication data to be indexed
     * @return a generator ready to make indexDocuments
     */
    public static ContentsDocumentGenerator fromJsonNode(JsonNode record) {
        String isbn = extractIsbn(record);
        ContentsDocumentBuilder builder = new ContentsDocumentBuilder()
                .withIsbn(isbn)
                .withTitle(extractTitle(record, isbn))
                .withAuthor(extractAuthor(record, isbn))
                .withYear(extractYear(record, isbn))
                .withShortDescription(extractShortDescription(record, isbn))
                .withLongDescription(extractLongDescription(record, isbn))
                .withTableOfContents(extractTableOfContents(record, isbn))
                .withSource(extractSource(record, isbn))
                .withSmallImage(extractImageUrlSmall(record, isbn))
                .withLargeImage(extractImageUrlLarge(record, isbn))
                .withOriginalImage(extractImageUrlOriginal(record, isbn))
                .withModified(extractModifiedDate(record, isbn))
                .withCreated(extractCreatedDate(record, isbn));

        return new ContentsDocumentGenerator(builder);
    }

    private static String extractAuthor(JsonNode record, String id) {
        var author = textFromNode(record, AUTHOR_JSON_POINTER);
        if (isNull(author)) {
            logMissingField(id, AUTHOR);
        }
        return author;
    }

    private static String extractIsbn(JsonNode record) {
        var title = textFromNode(record, ISBN_JSON_POINTER);
        if (isNull(title)) {
            logMissingField(null, ISBN);
        }
        return title;
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
    private static String extractImageUrlSmall(JsonNode record, String id) {
        var url = textFromNode(record, IMAGE_URL_SMALL_JSON_POINTER);
        if (isNull(url)) {
            logMissingField(id, IMAGE_URL_SMALL);
        }
        return url;
    }

    @JacocoGenerated
    private static String extractImageUrlLarge(JsonNode record, String id) {
        var url = textFromNode(record, IMAGE_URL_LARGE_JSON_POINTER);
        if (isNull(url)) {
            logMissingField(id, IMAGE_URL_LARGE);
        }
        return url;
    }

    @JacocoGenerated
    private static String extractImageUrlOriginal(JsonNode record, String id) {
        var url = textFromNode(record, IMAGE_URL_ORIGINAL_JSON_POINTER);
        if (isNull(url)) {
            logMissingField(id, IMAGE_URL_ORIGINAL);
        }
        return url;
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

}
