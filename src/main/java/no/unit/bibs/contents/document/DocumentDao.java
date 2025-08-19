package no.unit.bibs.contents.document;


import org.apache.commons.text.StringEscapeUtils;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;

import static no.unit.bibs.contents.StringHelper.isValidHtmlEscapeCode;

@SuppressWarnings("PMD.TooManyFields")
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
            builder.titleText,
            builder.authorName,
            builder.publicationDate,
            builder.isbnValue,
            builder.shortDescription,
            builder.longDescription,
            builder.toc,
            builder.promotionalText,
            builder.summaryText,
            builder.reviewText,
            builder.imageSmallUri,
            builder.imageLargeUri,
            builder.imageOriginalUri,
            builder.audioFileUri,
            builder.sourceText,
            builder.modifiedDate,
            builder.createdDate
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
        private String titleText;
        private String authorName;
        private String publicationDate;
        private String isbnValue;
        private String shortDescription;
        private String longDescription;
        private String toc;
        private String promotionalText;
        private String summaryText;
        private String reviewText;
        private String imageSmallUri;
        private String imageLargeUri;
        private String imageOriginalUri;
        private String audioFileUri;
        private String sourceText;
        private Instant modifiedDate;
        private Instant createdDate;

        public Builder title(String title) {
            this.titleText =  isValidHtmlEscapeCode(title)
                ? title
                : StringEscapeUtils.unescapeHtml4(title);
            return this;
        }

        public Builder author(String author) {
            this.authorName = isValidHtmlEscapeCode(author)
                ? author
                : StringEscapeUtils.unescapeHtml4(author);
            return this;
        }

        public Builder dateOfPublication(String dateOfPublication) {
            this.publicationDate = dateOfPublication;
            return this;
        }

        public Builder isbn(String isbn) {
            this.isbnValue = isbn;
            return this;
        }

        public Builder descriptionShort(String descriptionShort) {
            this.shortDescription = isValidHtmlEscapeCode(descriptionShort)
                ? descriptionShort
                : StringEscapeUtils.unescapeHtml4(descriptionShort);
            return this;
        }

        public Builder descriptionLong(String descriptionLong) {
            this.longDescription = isValidHtmlEscapeCode(descriptionLong)
                ? descriptionLong
                : StringEscapeUtils.unescapeHtml4(descriptionLong);
            return this;
        }

        public Builder tableOfContents(String tableOfContents) {
            this.toc = isValidHtmlEscapeCode(tableOfContents)
                ? tableOfContents
                : StringEscapeUtils.unescapeHtml4(tableOfContents);
            return this;
        }

        public Builder promotional(String promotional) {
            this.promotionalText = isValidHtmlEscapeCode(promotional)
                ? promotional
                : StringEscapeUtils.unescapeHtml4(promotional);
            return this;
        }

        public Builder summary(String summary) {
            this.summaryText = isValidHtmlEscapeCode(summary)
                ? summary
                : StringEscapeUtils.unescapeHtml4(summary);
            return this;
        }

        public Builder review(String review) {
            this.reviewText = isValidHtmlEscapeCode(review)
                ? review
                : StringEscapeUtils.unescapeHtml4(review);
            return this;
        }

        public Builder imageSmall(String imageSmall) {
            this.imageSmallUri = imageSmall;
            return this;
        }

        public Builder imageLarge(String imageLarge) {
            this.imageLargeUri = imageLarge;
            return this;
        }

        public Builder imageOriginal(String imageOriginal) {
            this.imageOriginalUri = imageOriginal;
            return this;
        }

        public Builder audioFile(String audioFile) {
            this.audioFileUri = audioFile;
            return this;
        }

        public Builder source(String source) {
            this.sourceText = source;
            return this;
        }

        public Builder modified(Instant modified) {
            this.modifiedDate = modified;
            return this;
        }

        public Builder created(Instant created) {
            this.createdDate = created;
            return this;
        }


        public DocumentDao build() {
            return new DocumentDao(this);
        }
    }
}
