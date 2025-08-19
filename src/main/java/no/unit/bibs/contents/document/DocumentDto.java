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
        if (isBlank(isbn) || isBlank(source)) {
            return false;
        }
        var anyDescription = nullToBlank(descriptionShort)
            + nullToBlank(descriptionLong)
            + nullToBlank(tableOfContents)
            + nullToBlank(author)
            + nullToBlank(summary)
            + nullToBlank(review)
            + nullToBlank(promotional);
        var anyImage = nullToBlank(imageSmall) + nullToBlank(imageLarge) + nullToBlank(imageOriginal);
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
    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }

}
