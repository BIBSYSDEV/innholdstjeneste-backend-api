package no.unit.bibs.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nva.commons.json.JsonSerializable;
import nva.commons.utils.JacocoGenerated;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class IndexDocument implements JsonSerializable {

    private final UUID id;
    private final List<String> contributors;
    private final String title;
    private final Instant year;
    private final List<String> isbn;
    private final String descriptionShort;
    private final String descriptionLong;
    private final String tableOfContents;
    private final URI imageUrlSmall;
    private final URI imageUrlLarge;
    private final URI imageUrlOriginal;
    private final String source;
    private final Instant modifiedDate;
    private final Instant createdDate;

    /**
     * Creates and IndexDocument with given properties.
     */
    @JacocoGenerated
    @JsonCreator
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public IndexDocument(@JsonProperty("id") UUID id,
                         @JsonProperty("title") String title,
                         @JsonProperty("contributors") List<String> contributors,
                         @JsonProperty("year") Instant year,
                         @JsonProperty("isbn") List<String> isbnList,
                         @JsonProperty("description_short") String descriptionShort,
                         @JsonProperty("description_long") String descriptionLong,
                         @JsonProperty("tableOfContents") String tableOfContents,
                         @JsonProperty("image_url_small") URI imageUrlSmall,
                         @JsonProperty("image_url_large") URI imageUrlLarge,
                         @JsonProperty("image_url_original") URI imageUrlOriginal,
                         @JsonProperty("source") String source,
                         @JsonProperty("modifiedDate") Instant modifiedDate,
                         @JsonProperty("createdDate") Instant createdDate) {
        this.id = id;
        this.title = title;
        this.contributors = contributors;
        this.year = year;
        this.isbn = isbnList;
        this.descriptionShort = descriptionShort;
        this.descriptionLong = descriptionLong;
        this.tableOfContents = tableOfContents;
        this.imageUrlSmall = imageUrlSmall;
        this.imageUrlLarge = imageUrlLarge;
        this.imageUrlOriginal = imageUrlOriginal;
        this.source = source;
        this.modifiedDate = modifiedDate;
        this.createdDate = createdDate;
    }

    protected IndexDocument(IndexDocumentBuilder builder) {
        id = builder.id;
        title = builder.title;
        contributors = builder.contributors;
        year = builder.year;
        isbn = builder.isbn;
        descriptionShort = builder.descriptionShort;
        descriptionLong = builder.descriptionLong;
        tableOfContents = builder.tableOfContents;
        imageUrlSmall = builder.imageUrlSmall;
        imageUrlLarge = builder.imageUrlLarge;
        imageUrlOriginal = builder.imageUrlOriginal;
        source = builder.source;
        modifiedDate = builder.modifiedDate;
        createdDate = builder.createdDate;
    }

    @JacocoGenerated
    public UUID getId() {
        return id;
    }

    @JacocoGenerated
    public List<String> getContributors() {
        return contributors;
    }

    @JacocoGenerated
    public String getTitle() {
        return title;
    }

    @JacocoGenerated
    public Instant getModifiedDate() {
        return modifiedDate;
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IndexDocument)) {
            return false;
        }
        IndexDocument that = (IndexDocument) o;
        return Objects.equals(id, that.id)
            && Objects.equals(contributors, that.contributors)
            && Objects.equals(title, that.title)
            && Objects.equals(year, that.year)
            && Objects.equals(modifiedDate, that.modifiedDate)
            && Objects.equals(createdDate, that.createdDate)
            && Objects.equals(descriptionShort, that.descriptionShort)
            && Objects.equals(descriptionLong, that.descriptionLong)
            && Objects.equals(source, that.source)
            && Objects.equals(isbn, that.isbn);
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(id,
                contributors,
                title,
                year,
                isbn,
                descriptionShort,
                descriptionLong,
                tableOfContents,
                imageUrlSmall,
                imageUrlLarge,
                imageUrlOriginal,
                modifiedDate,
                createdDate,
                source);
    }

    @JacocoGenerated
    @Override
    public String toString() {
        return toJsonString();
    }

}
