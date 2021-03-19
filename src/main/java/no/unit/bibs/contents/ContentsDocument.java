package no.unit.bibs.contents;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nva.commons.json.JsonSerializable;
import nva.commons.utils.JacocoGenerated;

import java.time.Instant;
import java.util.Objects;

public class ContentsDocument implements JsonSerializable {

    public static final String TITLE = "title";
    public static final String AUTHOR = "author";
    public static final String DATE_OF_PUBLICATION = "date_of_publication";
    public static final String ISBN = "isbn";
    public static final String DESCRIPTION_SHORT = "description_short";
    public static final String DESCRIPTION_LONG = "description_long";
    public static final String TABLE_OF_CONTENTS = "table_of_contents";
    public static final String IMAGE_SMALL = "image_small";
    public static final String IMAGE_LARGE = "image_large";
    public static final String IMAGE_ORIGINAL = "image_original";
    public static final String AUDIO_FILE = "audio_file";
    public static final String SOURCE = "source";
    public static final String MODIFIED = "modified";
    public static final String CREATED = "created";
    private final String author;
    private final String title;
    private final String dateOfPublication;
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
    public ContentsDocument(@JsonProperty(TITLE) String title,
                            @JsonProperty(AUTHOR) String author,
                            @JsonProperty(DATE_OF_PUBLICATION) String dateOfPublication,
                            @JsonProperty(ISBN) String isbn,
                            @JsonProperty(DESCRIPTION_SHORT) String descriptionShort,
                            @JsonProperty(DESCRIPTION_LONG) String descriptionLong,
                            @JsonProperty(TABLE_OF_CONTENTS) String tableOfContents,
                            @JsonProperty(IMAGE_SMALL) String imageSmall,
                            @JsonProperty(IMAGE_LARGE) String imageLarge,
                            @JsonProperty(IMAGE_ORIGINAL) String imageOriginal,
                            @JsonProperty(AUDIO_FILE) String audioFile,
                            @JsonProperty(SOURCE) String source,
                            @JsonProperty(MODIFIED) Instant modified,
                            @JsonProperty(CREATED) Instant created) {
        this.title = title;
        this.author = author;
        this.dateOfPublication = dateOfPublication;
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

    public String getDateOfPublication() {
        return dateOfPublication;
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
            && Objects.equals(dateOfPublication, that.dateOfPublication)
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
                dateOfPublication,
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
