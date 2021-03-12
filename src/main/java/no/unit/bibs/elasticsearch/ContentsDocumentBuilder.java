package no.unit.bibs.elasticsearch;

import java.time.Instant;

public class ContentsDocumentBuilder {

    protected String title;
    protected String author;
    protected Instant year;
    protected String isbn;
    protected String descriptionShort;
    protected String descriptionLong;
    protected String tableOfContents;
    protected String imageSmall;
    protected String imageLarge;
    protected String imageOriginal;
    protected String audioFile;
    protected String source;
    protected Instant modified;
    protected Instant created;

    public ContentsDocumentBuilder() {
    }

    public ContentsDocumentBuilder withAuthor(String author) {
        this.author = author;
        return this;
    }

    public ContentsDocumentBuilder withIsbn(String isbn) {
        this.isbn = isbn;
        return this;
    }

    public ContentsDocumentBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public ContentsDocumentBuilder withYear(Instant year) {
        this.year = year;
        return this;
    }

    public ContentsDocumentBuilder withShortDescription(String descriptionShort) {
        this.descriptionShort = descriptionShort;
        return this;
    }

    public ContentsDocumentBuilder withLongDescription(String descriptionLong) {
        this.descriptionLong = descriptionLong;
        return this;
    }

    public ContentsDocumentBuilder withTableOfContents(String tableOfContents) {
        this.tableOfContents = tableOfContents;
        return this;
    }

    public ContentsDocumentBuilder withSmallImage(String imageUrlSmall) {
        this.imageSmall = imageUrlSmall;
        return this;
    }

    public ContentsDocumentBuilder withLargeImage(String imageUrlLarge) {
        this.imageLarge = imageUrlLarge;
        return this;
    }

    public ContentsDocumentBuilder withOriginalImage(String imageUrlOriginal) {
        this.imageOriginal = imageUrlOriginal;
        return this;
    }

    public ContentsDocumentBuilder withAudioFile(String audioFile) {
        this.audioFile = audioFile;
        return this;
    }

    public ContentsDocumentBuilder withSource(String source) {
        this.source = source;
        return this;
    }

    public ContentsDocumentBuilder withModified(Instant modified) {
        this.modified = modified;
        return this;
    }

    public ContentsDocumentBuilder withCreated(Instant created) {
        this.created = created;
        return this;
    }

    public ContentsDocument build() {
        return new ContentsDocument(this);
    }
}
