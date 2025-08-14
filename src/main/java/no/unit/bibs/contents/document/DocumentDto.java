package no.unit.bibs.contents.document;

import no.unit.nva.commons.json.JsonSerializable;

import java.time.Instant;

import static nva.commons.core.StringUtils.isBlank;
import static nva.commons.core.StringUtils.isNotBlank;

public record DocumentDto(
    String title,
    String author,
    String dateOfPublication,
    String isbn,
    String descriptionShort,
    String descriptionLong,
    String tableOfContents,
    String promotional,
    String summary,
    String review,
    String imageSmall,
    String imageLarge,
    String imageOriginal,
    String audioFile,
    String source,
    Instant modified,
    Instant created) implements JsonSerializable {

    boolean isValid() {
        if (isBlank(isbn)) {
            return false;
        }
        if (isBlank(source)) {
            return false;
        }
        var anyDescription = n2b(descriptionShort)
            + n2b(descriptionLong)
            + n2b(tableOfContents)
            + n2b(author)
            + n2b(summary)
            + n2b(review)
            + n2b(promotional);
        var anyImage = n2b(imageSmall) + n2b(imageLarge) + n2b(imageOriginal);
        return isNotBlank(anyDescription) || isNotBlank(anyImage);
    }

    public DocumentDao.Builder toDaoBuilder() {
        return DocumentDao.builder()
            .title(title)
            .author(author)
            .dateOfPublication(dateOfPublication)
            .isbn(isbn)
            .descriptionShort(descriptionShort)
            .descriptionLong(descriptionLong)
            .promotional(promotional)
            .summary(summary)
            .review(review)
            .imageSmall(imageSmall)
            .imageLarge(imageLarge)
            .imageOriginal(imageOriginal)
            .audioFile(audioFile)
            .source(source)
            .modified(modified)
            .created(created);
    }

    /**
     * Converts null to blank string.
     *
     * @param value the value to convert
     * @return empty string if value is null, otherwise the original value
     */
    private String n2b(String value) {
        return value == null ? "" : value;
    }

}
