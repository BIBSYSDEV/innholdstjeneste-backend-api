package no.unit.bibs.contents;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.JacocoGenerated;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.Objects;

@SuppressWarnings("PMD.TooManyFields")
public class ContentsDocument implements JsonSerializable {

    public static final String TITLE = "title";
    public static final String AUTHOR = "author";
    public static final String DATE_OF_PUBLICATION = "date_of_publication";
    public static final String ISBN = "isbn";
    public static final String DESCRIPTION_SHORT = "description_short";
    public static final String DESCRIPTION_LONG = "description_long";
    public static final String TABLE_OF_CONTENTS = "table_of_contents";
    public static final String PROMOTIONAL = "promotional";
    public static final String SUMMARY = "summary";
    public static final String REVIEW = "review";
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
    private final String promotional;
    private final String summary;
    private final String review;
    private String imageSmall;
    private String imageLarge;
    private String imageOriginal;
    private String audioFile;
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
                            @JsonProperty(PROMOTIONAL) String promotional,
                            @JsonProperty(SUMMARY) String summary,
                            @JsonProperty(REVIEW) String review,
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
        this.promotional = promotional;
        this.summary = summary;
        this.review = review;
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
    public String getPromotional() {
        return promotional;
    }

    @JacocoGenerated
    public String getSummary() {
        return summary;
    }

    @JacocoGenerated
    public String getReview() {
        return review;
    }

    @JacocoGenerated
    public String getImageSmall() {
        return imageSmall;
    }

    @JacocoGenerated
    public String getDateOfPublication() {
        return dateOfPublication;
    }

    @JacocoGenerated
    public String getImageLarge() {
        return imageLarge;
    }

    @JacocoGenerated
    public String getImageOriginal() {
        return imageOriginal;
    }

    @JacocoGenerated
    public String getAudioFile() {
        return audioFile;
    }

    @JacocoGenerated
    public String getSource() {
        return source;
    }

    @JacocoGenerated
    public String getIsbn() {
        return isbn;
    }

    @JacocoGenerated
    public Instant getCreated() {
        return created;
    }

    @JacocoGenerated
    public Instant getModified() {
        return modified;
    }

    @JacocoGenerated
    public void setImageSmall(String imageSmall) {
        this.imageSmall = imageSmall;
    }

    @JacocoGenerated
    public void setImageLarge(String imageLarge) {
        this.imageLarge = imageLarge;
    }

    @JacocoGenerated
    public void setImageOriginal(String imageOriginal) {
        this.imageOriginal = imageOriginal;
    }

    @JacocoGenerated
    public void setAudioFile(String audioFile) {
        this.audioFile = audioFile;
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


    protected boolean isValid() {
        if (StringUtils.isBlank(isbn)) {
            return false;
        }
        if (StringUtils.isBlank(source)) {
            return false;
        }
        StringBuilder tempDesc = new StringBuilder();
        tempDesc.append(descriptionShort)
                .append(descriptionLong)
                .append(tableOfContents)
                .append(author)
                .append(summary)
                .append(review)
                .append(promotional);
        StringBuilder tempImg = new StringBuilder();
        tempImg.append(imageSmall)
                .append(imageLarge)
                .append(imageOriginal);
        return StringUtils.isNotBlank(tempDesc.toString()) || StringUtils.isNotBlank(tempImg.toString());
    }

}
