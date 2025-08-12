package no.unit.bibs.contents.document;


import org.apache.commons.text.StringEscapeUtils;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;

import static no.unit.bibs.contents.StringHelper.isValidHtmlEscapeCode;

@DynamoDbImmutable(builder = DocumentDao.Builder.class)
public record DocumentDao(
    String title,
    String author,
    String date_of_publication,
    @DynamoDbPartitionKey
    String isbn,
    String description_short,
    String description_long,
    String table_of_contents,
    String promotional,
    String summary,
    String review,
    String image_small,
    String image_large,
    String image_original,
    String audio_file,
    String source,
    Instant modified,
    @DynamoDbSortKey
    Instant created
) {

    private DocumentDao(Builder builder) {
        this(
            builder.title,
            builder.author,
            builder.dateOfPublication,
            builder.isbn,
            builder.descriptionShort,
            builder.descriptionLong,
            builder.tableOfContents,
            builder.promotional,
            builder.summary,
            builder.review,
            builder.imageSmall,
            builder.imageLarge,
            builder.imageOriginal,
            builder.audioFile,
            builder.source,
            builder.modified,
            builder.created
        );
    }

    public DocumentDto toDto() {
        return new DocumentDto(
            title,
            author,
            date_of_publication,
            isbn,
            description_short,
            description_long,
            table_of_contents,
            promotional,
            summary,
            review,
            image_small,
            image_large,
            image_original,
            audio_file,
            source,
            modified,
            created
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String title;
        private String author;
        private String dateOfPublication;
        private String isbn;
        private String descriptionShort;
        private String descriptionLong;
        private String tableOfContents;
        private String promotional;
        private String summary;
        private String review;
        private String imageSmall;
        private String imageLarge;
        private String imageOriginal;
        private String audioFile;
        private String source;
        private Instant modified;
        private Instant created;

        public Builder title(String title) {
            this.title =  isValidHtmlEscapeCode(title)
                ? title
                : StringEscapeUtils.unescapeHtml4(title);
            return this;
        }

        public Builder author(String author) {
            this.author = isValidHtmlEscapeCode(author)
                ? author
                : StringEscapeUtils.unescapeHtml4(author);
            return this;
        }

        public Builder dateOfPublication(String dateOfPublication) {
            this.dateOfPublication = dateOfPublication;
            return this;
        }

        public Builder isbn(String isbn) {
            this.isbn = isbn;
            return this;
        }

        public Builder descriptionShort(String descriptionShort) {
            this.descriptionShort = isValidHtmlEscapeCode(descriptionShort)
                ? descriptionShort
                : StringEscapeUtils.unescapeHtml4(descriptionShort);
            return this;
        }

        public Builder descriptionLong(String descriptionLong) {
            this.descriptionLong = isValidHtmlEscapeCode(descriptionLong)
                ? descriptionLong
                : StringEscapeUtils.unescapeHtml4(descriptionLong);
            return this;
        }

        public Builder tableOfContents(String tableOfContents) {
            this.tableOfContents = isValidHtmlEscapeCode(tableOfContents)
                ? tableOfContents
                : StringEscapeUtils.unescapeHtml4(tableOfContents);
            return this;
        }

        public Builder promotional(String promotional) {
            this.promotional = isValidHtmlEscapeCode(promotional)
                ? promotional
                : StringEscapeUtils.unescapeHtml4(promotional);
            return this;
        }

        public Builder summary(String summary) {
            this.summary = isValidHtmlEscapeCode(summary)
                ? summary
                : StringEscapeUtils.unescapeHtml4(summary);
            return this;
        }

        public Builder review(String review) {
            this.review = isValidHtmlEscapeCode(review)
                ? review
                : StringEscapeUtils.unescapeHtml4(review);
            return this;
        }

        public Builder imageSmall(String imageSmall) {
            this.imageSmall = imageSmall;
            return this;
        }

        public Builder imageLarge(String imageLarge) {
            this.imageLarge = imageLarge;
            return this;
        }

        public Builder imageOriginal(String imageOriginal) {
            this.imageOriginal = imageOriginal;
            return this;
        }

        public Builder audioFile(String audioFile) {
            this.audioFile = audioFile;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder modified(Instant modified) {
            this.modified = modified;
            return this;
        }

        public Builder created(Instant created) {
            this.created = created;
            return this;
        }


        public DocumentDao build() {
            return new DocumentDao(this);
        }
    }
}
