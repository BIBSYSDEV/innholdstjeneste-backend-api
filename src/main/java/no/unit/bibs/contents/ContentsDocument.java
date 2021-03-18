package no.unit.bibs.contents;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nva.commons.json.JsonSerializable;
import nva.commons.utils.JacocoGenerated;

import java.time.Instant;
import java.util.Objects;

public class ContentsDocument implements JsonSerializable {

    private final String author;
    private final String title;
    private final String year;
    private final String isbn;
    private final String descriptionShort;
    private final String descriptionLong;
    private final String tableOfContents;
    private final String imageSmall;
    private final String imageLarge;
    private final String imageOriginal;
    private final String audioFile;
    private final String source;
    private final Instant modified;
    private final Instant created;

    /**
     * Creates and IndexDocument with given properties.
     */
    @JacocoGenerated
    @JsonCreator
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public ContentsDocument(@JsonProperty("title") String title,
                            @JsonProperty("author") String author,
                            @JsonProperty("year") String year,
                            @JsonProperty("isbn") String isbn,
                            @JsonProperty("description_short") String descriptionShort,
                            @JsonProperty("description_long") String descriptionLong,
                            @JsonProperty("tableOfContents") String tableOfContents,
                            @JsonProperty("image_small") String imageSmall,
                            @JsonProperty("image_large") String imageLarge,
                            @JsonProperty("image_original") String imageOriginal,
                            @JsonProperty("audioFile") String audioFile,
                            @JsonProperty("source") String source,
                            @JsonProperty("modified") Instant modified,
                            @JsonProperty("created") Instant created) {
        this.title = title;
        this.author = author;
        this.year = year;
        this.isbn = isbn;
        this.descriptionShort = descriptionShort;
        this.descriptionLong = descriptionLong;
        this.tableOfContents = tableOfContents;
        this.imageSmall = imageSmall;
        this.imageLarge = imageLarge;
        this.imageOriginal = imageOriginal;
        this.audioFile = audioFile;
        this.source = source;
        this.modified = modified;
        this.created = created;
    }

    protected ContentsDocument(ContentsDocumentBuilder builder) {
        title = builder.title;
        author = builder.author;
        year = builder.year;
        isbn = builder.isbn;
        descriptionShort = builder.descriptionShort;
        descriptionLong = builder.descriptionLong;
        tableOfContents = builder.tableOfContents;
        imageSmall = builder.imageSmall;
        imageLarge = builder.imageLarge;
        imageOriginal = builder.imageOriginal;
        audioFile = builder.audioFile;
        source = builder.source;
        modified = builder.modified;
        created = builder.created;
    }

    @JacocoGenerated
    public String getAuthor() {
        return author;
    }

    @JacocoGenerated
    public String getTitle() {
        return title;
    }

    @JacocoGenerated
    public String getDescriptionShort() {
        return descriptionShort;
    }

    @JacocoGenerated
    public String getDescriptionLong() {
        return descriptionLong;
    }

    @JacocoGenerated
    public String getTableOfContents() {
        return tableOfContents;
    }

    @JacocoGenerated
    public String getImageSmall() {
        return imageSmall;
    }

    public String getYear() {
        return year;
    }

    public String getImageLarge() {
        return imageLarge;
    }

    public String getImageOriginal() {
        return imageOriginal;
    }

    public String getAudioFile() {
        return audioFile;
    }

    public String getSource() {
        return source;
    }

    @JacocoGenerated
    public String getIsbn() {
        return isbn;
    }

    @JacocoGenerated
    public Instant getModified() {
        return modified;
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContentsDocument)) {
            return false;
        }
        ContentsDocument that = (ContentsDocument) o;
        return Objects.equals(author, that.author)
            && Objects.equals(title, that.title)
            && Objects.equals(year, that.year)
            && Objects.equals(modified, that.modified)
            && Objects.equals(created, that.created)
            && Objects.equals(descriptionShort, that.descriptionShort)
            && Objects.equals(descriptionLong, that.descriptionLong)
            && Objects.equals(source, that.source)
            && Objects.equals(isbn, that.isbn);
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(author,
                title,
                year,
                isbn,
                descriptionShort,
                descriptionLong,
                tableOfContents,
                imageSmall,
                imageLarge,
                imageOriginal,
                modified,
                created,
                source);
    }

    @JacocoGenerated
    @Override
    public String toString() {
        return toJsonString();
    }

}
