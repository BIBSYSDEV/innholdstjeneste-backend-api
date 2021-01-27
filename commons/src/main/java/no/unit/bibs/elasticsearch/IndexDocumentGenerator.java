package no.unit.bibs.elasticsearch;

import com.amazonaws.services.dynamodbv2.document.ItemUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.unit.nva.model.Reference;
import no.unit.nva.utils.DynamodbItemUtilsClone;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    public static final String CONTRIBUTOR_LIST_JSON_POINTER = "/entityDescription/m/contributors/l";
    public static final String CONTRIBUTOR_ID_JSON_POINTER = "/m/identity/m/id/s";
    public static final String CONTRIBUTOR_NAME_JSON_POINTER = "/m/identity/m/name/s";
    public static final String IDENTIFIER_JSON_POINTER = "/identifier/s";
    public static final String MAIN_TITLE_JSON_POINTER = "/entityDescription/m/mainTitle/s";
    public static final String TYPE_JSON_POINTER = "/entityDescription/m/reference/m/publicationInstance/m/type/s";
    public static final String DOI_JSON_POINTER = "/entityDescription/m/reference/m/doi/s";
    public static final String OWNER_JSON_POINTER = "/owner/s";
    public static final String DESCRIPTION_JSON_POINTER = "/entityDescription/m/description/s";
    public static final String PUBLICATION_ABSTRACT_JSON_POINTER = "/entityDescription/m/abstract/s";
    public static final String PUBLISHER_ID_JSON_POINTER = "/publisher/m/id/s";
    public static final String PUBLISHER_TYPE_JSON_POINTER = "/publisher/m/type/s";
    public static final String MODIFIED_DATE_JSON_POINTER = "/modifiedDate/s";
    public static final String PUBLISHED_DATE_JSON_POINTER = "/publishedDate/s";
    public static final String ALTERNATIVE_TITLES_JSON_POINTER = "/entityDescription/m/alternativeTitles/m";
    public static final String TAGS_LIST_JSON_POINTER = "/entityDescription/m/tags/l";
    public static final String REFERENCE_JSON_POINTER = "/entityDescription/m/reference";

    public static final String MISSING_FIELD_LOGGER_WARNING_TEMPLATE =
            "The data from DynamoDB was incomplete, missing required field {} on id: {}, ignoring entry";
    public static final String DATE_FIELD_FORMAT_ERROR_LOGGER_WARNING_TEMPLATE =
            "The data from DynamoDB was incorrect, field {} on id: {}, ignoring value {}";
    public static final String EXCEPTION_READING_DOI_MESSAGE = "Exception reading DOI, recordId={}";
    public static final String JSON_PROCESSING_EXCEPTION_ON_FIELD_REFERENCE =
            "JsonProcessingException on field 'reference' in record with id={}";

    public static final String PUBLISHED = "PUBLISHED";
    public static final String STATUS = "status";
    public static final String TYPE = "type";
    public static final String TITLE = "title";
    public static final String OWNER = "owner";
    public static final String DESCRIPTION = "description";
    public static final String ABSTRACT = "abstract";
    public static final String MODIFIED_DATE = "modifiedDate";
    public static final String PUBLISHED_DATE = "publishedDate";
    public static final String ALTERNATIVE_TITLES = "alternativeTitles";
    public static final String TAGS = "tags";

    private static final ObjectMapper mapper = JsonUtils.objectMapper;
    public static final JavaType ATTRIBUTE_VALUE_JAVA_TYPE =
            mapper.getTypeFactory().constructParametricType(Map.class,
            String.class,
            AttributeValue.class);
    public static final JavaType LIST_ATTRIBUTE_VALUE_JAVA_TYPE =
            mapper.getTypeFactory().constructParametricType(List.class, AttributeValue.class);

    private static final Logger logger = LoggerFactory.getLogger(IndexDocumentGenerator.class);

    @JacocoGenerated
    private IndexDocumentGenerator(IndexDocument.Builder builder) {
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

        Builder builder = new Builder()
                .withId(id)
                .withType(extractType(record, id))
                .withContributors(extractContributors(record))
                .withPublicationDate(new IndexDate(record))
                .withTitle(extractTitle(record, id))
                .withOwner(extractOwner(record, id))
                .withDescription(extractDescription(record, id))
                .withAbstract(extractAbstract(record, id))
                .withPublisher(extractPublisher(record))
                .withModifiedDate(extractModifiedDate(record, id))
                .withPublishedDate(extractPublishedDate(record, id))
                .withAlternativeTitles(extractAlternativeTitles(record, id))
                .withTags(extractTags(record,id))
                .withReference(extractDescriptionReference(record,id));

        Optional<URI> optionalURI = extractDoi(record);
        optionalURI.ifPresent(builder::withDoi);

        return new IndexDocumentGenerator(builder);
    }

    private static List<IndexContributor> extractContributors(JsonNode record) {
        return toStream(record.at(CONTRIBUTOR_LIST_JSON_POINTER))
                .map(IndexDocumentGenerator::extractIndexContributor)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static IndexContributor extractIndexContributor(JsonNode jsonNode) {
        Optional<URI> contributorId = extractContributorId(jsonNode);
        String name = textFromNode(jsonNode, CONTRIBUTOR_NAME_JSON_POINTER);
        return nonNull(name) ? generateIndexContributor(contributorId, name) : null;
    }

    private static UUID extractId(JsonNode record) {
        return Optional.ofNullable(record)
                .map(rec -> textFromNode(rec, IDENTIFIER_JSON_POINTER))
                .map(UUID::fromString)
                .orElseThrow();
    }

    private static String extractTitle(JsonNode record, UUID id) {
        var title = textFromNode(record, MAIN_TITLE_JSON_POINTER);
        if (isNull(title)) {
            logMissingField(id, TITLE);
        }
        return title;
    }

    private static String extractType(JsonNode record, UUID id) {
        var type = textFromNode(record, TYPE_JSON_POINTER);
        if (isNull(type)) {
            logMissingField(id, TYPE);
        }
        return type;
    }

    @JacocoGenerated
    private static Optional<URI> extractDoi(JsonNode record) {
        String textFromNode = textFromNode(record, DOI_JSON_POINTER);
        try {
            if (!isEmpty(textFromNode)) {
                return Optional.of(getUri(textFromNode));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.warn(EXCEPTION_READING_DOI_MESSAGE, textFromNode(record, IDENTIFIER_JSON_POINTER));
            return Optional.empty();
        }
    }

    private static URI getUri(String textFromNode) throws URISyntaxException {
        return new URI(textFromNode);
    }

    private static URI extractPublisherId(JsonNode record) {
        return Optional.ofNullable(record)
                .map(rec -> textFromNode(rec, PUBLISHER_ID_JSON_POINTER))
                .map(URI::create)
                .orElseThrow();
    }

    private static Optional<URI> extractContributorId(JsonNode record) {
        return Optional.ofNullable(record)
                .map(rec -> textFromNode(rec, CONTRIBUTOR_ID_JSON_POINTER))
                .map(URI::create);
    }

    private static String extractOwner(JsonNode record, UUID id) {
        var owner = textFromNode(record, OWNER_JSON_POINTER);
        if (isNull(owner)) {
            logMissingField(id, OWNER);
        }
        return owner;
    }

    private static String extractDescription(JsonNode record, UUID id) {
        var description = textFromNode(record, DESCRIPTION_JSON_POINTER);
        if (isNull(description)) {
            logMissingField(id, DESCRIPTION);
        }
        return description;
    }

    private static String extractAbstract(JsonNode record, UUID id) {
        var publicationAbstract = textFromNode(record, PUBLICATION_ABSTRACT_JSON_POINTER);
        if (isNull(publicationAbstract)) {
            logMissingField(id, ABSTRACT);
        }
        return publicationAbstract;
    }

    private static IndexPublisher extractPublisher(JsonNode record) {
        URI publisherId = extractPublisherId(record);
        String publisherType = textFromNode(record, PUBLISHER_TYPE_JSON_POINTER);
        return nonNull(publisherId) ? generateIndexPublisher(publisherId, publisherType) : null;
    }

    private static Instant extractModifiedDate(JsonNode record, UUID id) {
        return getInstant(record, id, MODIFIED_DATE_JSON_POINTER, MODIFIED_DATE);
    }

    private static Instant extractPublishedDate(JsonNode record, UUID id) {
        return getInstant(record, id, PUBLISHED_DATE_JSON_POINTER, PUBLISHED_DATE);
    }

    private static Map<String, String> extractAlternativeTitles(JsonNode record, UUID id) {
        return getStringMap(record, id, ALTERNATIVE_TITLES_JSON_POINTER, ALTERNATIVE_TITLES);
    }

    private static List<String> extractTags(JsonNode record, UUID id) {
        return getStringList(record, id, TAGS_LIST_JSON_POINTER, TAGS);
    }

    private static Reference extractDescriptionReference(JsonNode record, UUID id) {
        Reference reference = null;
        try {
            JsonNode node = record.at(REFERENCE_JSON_POINTER);
            AttributeValue referenceAsAttributeValue = mapper.readValue(node.toString(), AttributeValue.class);
            var value = DynamodbItemUtilsClone.toSimpleValue(referenceAsAttributeValue);
            String valueAsJsonString = mapper.writeValueAsString(value);
            reference = mapper.readValue(valueAsJsonString, Reference.class);
        } catch (JsonProcessingException e) {
            logger.error(JSON_PROCESSING_EXCEPTION_ON_FIELD_REFERENCE,id);
        }
        return reference;
    }

    private static Map<String, String> getStringMap(JsonNode record, UUID id, String fieldJsonPtr, String fieldName) {
        Map<String, String> map = Collections.emptyMap();
        try {
            JsonNode node = record.at(fieldJsonPtr);
            Map<String, AttributeValue> attributeValueMap =
                    mapper.readValue(node.toString(), ATTRIBUTE_VALUE_JAVA_TYPE);
            map = ItemUtils.toSimpleMapValue(attributeValueMap);
        } catch (JsonProcessingException e) {
            logMissingField(id, fieldName);
        }
        return map;
    }

    private static List<String> getStringList(JsonNode record, UUID id, String fieldJsonPtr, String fieldName) {
        JsonNode node = record.at(fieldJsonPtr);
        List<String> list = Collections.emptyList();
        try {
            final String jString = node.toString();
            List<AttributeValue> attributeValueMap = mapper.readValue(jString, LIST_ATTRIBUTE_VALUE_JAVA_TYPE);
            list = ItemUtils.toSimpleListValue(attributeValueMap);
        } catch (JsonProcessingException e) {
            logMissingField(id, fieldName);
        }
        return list;
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

    private static IndexContributor generateIndexContributor(Optional<URI> id, String name) {
        IndexContributor.Builder builder = new IndexContributor.Builder().withName(name);
        if (id.isPresent()) {
            builder = builder.withId(id.get());
        }
        return builder.build();
    }

    private static IndexPublisher generateIndexPublisher(URI identifier, String type) {
        String name = type; // TODO sg - fix lookup of publisher name
        return new IndexPublisher.Builder()
                .withId(identifier)
                .withName(name)
                .build();
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
