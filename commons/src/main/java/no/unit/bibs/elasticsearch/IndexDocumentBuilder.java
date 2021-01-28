package no.unit.bibs.elasticsearch;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class IndexDocumentBuilder {

    protected UUID id;
    protected String title;
    protected List<String> contributors;
    protected Instant year;
    protected List<String> isbn;
    protected String descriptionShort;
    protected String descriptionLong;
    protected String tableOfContents;
    protected URI imageUrlSmall;
    protected URI imageUrlLarge;
    protected URI imageUrlOriginal;
    protected String source;
    protected Instant modifiedDate;
    protected Instant createdDate;

    public IndexDocumentBuilder() {
    }

    public IndexDocumentBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public IndexDocumentBuilder withContributors(List<String> contributors) {
        this.contributors = contributors;
        return this;
    }

    public IndexDocumentBuilder withIsbn(List<String> isbn) {
        this.isbn = isbn;
        return this;
    }

    public IndexDocumentBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public IndexDocumentBuilder withYear(Instant year) {
        this.year = year;
        return this;
    }

    public IndexDocumentBuilder withShortDescription(String descriptionShort) {
        this.descriptionShort = descriptionShort;
        return this;
    }

    public IndexDocumentBuilder withLongDescription(String descriptionLong) {
        this.descriptionLong = descriptionLong;
        return this;
    }

    public IndexDocumentBuilder withTableOfContents(String tableOfContents) {
        this.tableOfContents = tableOfContents;
        return this;
    }

    public IndexDocumentBuilder withSmallImage(URI imageUrlSmall) {
        this.imageUrlSmall = imageUrlSmall;
        return this;
    }

    public IndexDocumentBuilder withLargeImage(URI imageUrlLarge) {
        this.imageUrlLarge = imageUrlLarge;
        return this;
    }

    public IndexDocumentBuilder withOriginalImage(URI imageUrlOriginal) {
        this.imageUrlOriginal = imageUrlOriginal;
        return this;
    }

    public IndexDocumentBuilder withSource(String source) {
        this.source = source;
        return this;
    }

    public IndexDocumentBuilder withModifiedDate(Instant modifiedDate) {
        this.modifiedDate = modifiedDate;
        return this;
    }

    public IndexDocumentBuilder withCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public IndexDocument build() {
        return new IndexDocument(this);
    }
}
