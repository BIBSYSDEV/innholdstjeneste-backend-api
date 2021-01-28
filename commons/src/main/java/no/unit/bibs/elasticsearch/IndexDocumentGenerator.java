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
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static nva.commons.utils.StringUtils.isEmpty;

@SuppressWarnings("PMD.GodClass")
public final class IndexDocumentGenerator extends IndexDocument {

    public static final String CONTRIBUTOR_LIST_JSON_POINTER = "/contributors/l";
    public static final String ISBN_LIST_JSON_POINTER = "/isbns/l";
    public static final String CONTRIBUTOR_NAME_JSON_POINTER = "/m/identity/m/name/s";
    public static final String ISBN_JSON_POINTER = "/m/identity/m/isbn/s";
    public static final String IDENTIFIER_JSON_POINTER = "/identifier/s";
    public static final String TITLE_JSON_POINTER = "/title/s";
    public static final String YEAR_JSON_POINTER = "/year/s";
    public static final String IMAGE_URL_SMALL_JSON_POINTER = "/reference/m/imageUrlSmall/s";
    public static final String IMAGE_URL_LARGE_JSON_POINTER = "/reference/m/imageUrlLarge/s";
    public static final String IMAGE_URL_ORIGINAL_JSON_POINTER = "/reference/m/imageUrlOriginal/s";
    public static final String DESCRIPTION_SHORT_JSON_POINTER = "/descriptionShort/s";
    public static final String DESCRIPTION_LONG_JSON_POINTER = "/descriptionLong/s";
    public static final String TABLE_OF_CONTENTS_JSON_POINTER = "/tableOfContents/s";
    public static final String MODIFIED_DATE_JSON_POINTER = "/modifiedDate/s";
    public static final String CREATED_DATE_JSON_POINTER = "/createdDate/s";
    public static final String SOURCE_JSON_POINTER = "/source/s";

    public static final String MISSING_FIELD_LOGGER_WARNING_TEMPLATE =
            "The data were incomplete, missing field {} on id: {}, ignoring entry";
    public static final String DATE_FIELD_FORMAT_ERROR_LOGGER_WARNING_TEMPLATE =
            "The data from DynamoDB was incorrect, field {} on id: {}, ignoring value {}";
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
     * Transforms a DynamoDB streamRecord into IndexDocument.
     *
     * @param streamRecord of the original dynamoDB record
     * @return A document usable for indexing in elasticsearch
     */
    public static IndexDocumentGenerator fromStreamRecord(DynamodbEvent.DynamodbStreamRecord streamRecord) {
        JsonNode record = toJsonNode(streamRecord);
        return fromJsonNode(record);
    }

    /**
     * Initialise an IndexDocumentGenerator from an jsonNode.
     * @param record jsonNode containing publication data to be indexed
     * @return a generator ready to make indexDocuments
     */
    public static IndexDocumentGenerator fromJsonNode(JsonNode record) {
        UUID id = extractId(record);

        IndexDocumentBuilder builder = new IndexDocumentBuilder()
                .withId(id)
                .withTitle(extractTitle(record, id))
                .withContributors(extractContributors(record))
                .withYear(extractYear(record, id))
                .withIsbn(extractIsbnList(record))
                .withShortDescription(extractShortDescription(record, id))
                .withLongDescription(extractLongDescription(record, id))
                .withTableOfContents(extractTableOfContents(record, id))
                .withSource(extractSource(record, id))
                .withModifiedDate(extractModifiedDate(record, id))
                .withCreatedDate(extractCreatedDate(record, id));

        Optional<URI> optionalURIsmall = extractImageUrlSmall(record);
        optionalURIsmall.ifPresent(builder::withSmallImage);
        Optional<URI> optionalURIlarge = extractImageUrlLarge(record);
        optionalURIlarge.ifPresent(builder::withLargeImage);
        Optional<URI> optionalURIoriginal = extractImageUrlOrginal(record);
        optionalURIoriginal.ifPresent(builder::withOriginalImage);

        return new IndexDocumentGenerator(builder);
    }

    private static List<String> extractContributors(JsonNode record) {
        return toStream(record.at(CONTRIBUTOR_LIST_JSON_POINTER))
                .map(IndexDocumentGenerator::extractContributor)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static List<String> extractIsbnList(JsonNode record) {
        return toStream(record.at(ISBN_LIST_JSON_POINTER))
                .map(IndexDocumentGenerator::extractIsbn)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static String extractContributor(JsonNode jsonNode) {
        String name = textFromNode(jsonNode, CONTRIBUTOR_NAME_JSON_POINTER);
        return nonNull(name) ? name : null;
    }

    private static String extractIsbn(JsonNode jsonNode) {
        String name = textFromNode(jsonNode, ISBN_JSON_POINTER);
        return nonNull(name) ? name : null;
    }

    private static UUID extractId(JsonNode record) {
        return Optional.ofNullable(record)
                .map(rec -> textFromNode(rec, IDENTIFIER_JSON_POINTER))
                .map(UUID::fromString)
                .orElseThrow();
    }

    private static String extractTitle(JsonNode record, UUID id) {
        var title = textFromNode(record, TITLE_JSON_POINTER);
        if (isNull(title)) {
            logMissingField(id, TITLE);
        }
        return title;
    }

    private static Instant extractYear(JsonNode record, UUID id) {
        return getInstant(record, id, YEAR_JSON_POINTER, YEAR);
    }

    @JacocoGenerated
    private static Optional<URI> extractImageUrlSmall(JsonNode record) {
        String textFromNode = textFromNode(record, IMAGE_URL_SMALL_JSON_POINTER);
        try {
            if (!isEmpty(textFromNode)) {
                return Optional.of(getUri(textFromNode));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.warn(EXCEPTION_READING_IMAGE_URL_MESSAGE, textFromNode(record, IDENTIFIER_JSON_POINTER));
            return Optional.empty();
        }
    }

    @JacocoGenerated
    private static Optional<URI> extractImageUrlLarge(JsonNode record) {
        String textFromNode = textFromNode(record, IMAGE_URL_LARGE_JSON_POINTER);
        try {
            if (!isEmpty(textFromNode)) {
                return Optional.of(getUri(textFromNode));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.warn(EXCEPTION_READING_IMAGE_URL_MESSAGE, textFromNode(record, IDENTIFIER_JSON_POINTER));
            return Optional.empty();
        }
    }

    @JacocoGenerated
    private static Optional<URI> extractImageUrlOrginal(JsonNode record) {
        String textFromNode = textFromNode(record, IMAGE_URL_ORIGINAL_JSON_POINTER);
        try {
            if (!isEmpty(textFromNode)) {
                return Optional.of(getUri(textFromNode));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.warn(EXCEPTION_READING_IMAGE_URL_MESSAGE, textFromNode(record, IDENTIFIER_JSON_POINTER));
            return Optional.empty();
        }
    }

    private static URI getUri(String textFromNode) throws URISyntaxException {
        return new URI(textFromNode);
    }

    private static String extractShortDescription(JsonNode record, UUID id) {
        var description = textFromNode(record, DESCRIPTION_SHORT_JSON_POINTER);
        if (isNull(description)) {
            logMissingField(id, DESCRIPTION_SHORT);
        }
        return description;
    }

    private static String extractLongDescription(JsonNode record, UUID id) {
        var description = textFromNode(record, DESCRIPTION_LONG_JSON_POINTER);
        if (isNull(description)) {
            logMissingField(id, DESCRIPTION_LONG);
        }
        return description;
    }

    private static String extractTableOfContents(JsonNode record, UUID id) {
        var publicationAbstract = textFromNode(record, TABLE_OF_CONTENTS_JSON_POINTER);
        if (isNull(publicationAbstract)) {
            logMissingField(id, TABLE_OF_CONTENTS);
        }
        return publicationAbstract;
    }

    private static String extractSource(JsonNode record, UUID id) {
        var publicationAbstract = textFromNode(record, SOURCE_JSON_POINTER);
        if (isNull(publicationAbstract)) {
            logMissingField(id, SOURCE);
        }
        return publicationAbstract;
    }

    private static Instant extractModifiedDate(JsonNode record, UUID id) {
        return getInstant(record, id, MODIFIED_DATE_JSON_POINTER, MODIFIED_DATE);
    }

    private static Instant extractCreatedDate(JsonNode record, UUID id) {
        return getInstant(record, id, CREATED_DATE_JSON_POINTER, CREATED_DATE);
    }

    @JacocoGenerated
    private static Instant getInstant(JsonNode record, UUID id, String fieldJsonPtr, String fieldName) {
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

    private static void logMissingField(UUID id, String field) {
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
