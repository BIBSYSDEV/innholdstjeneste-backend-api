package no.unit.bibs.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.unit.nva.model.Reference;
import nva.commons.json.JsonSerializable;
import nva.commons.utils.JacocoGenerated;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.nonNull;

public class IndexDocument implements JsonSerializable {

    private final String publicationType;
    private final UUID id;
    private final URI doi;
    private final List<IndexContributor> contributors;
    private final String title;
    private final String publicationAbstract;
    private final String description;
    private final String owner;
    private final IndexDate publicationDate;
    private final IndexPublisher publisher;
    private final Instant modifiedDate;
    private final Instant publishedDate;
    private final Map<String, String> alternativeTitles;
    private final List<String> tags;
    private final Reference reference;

    /**
     * Creates and IndexDocument with given properties.
     */
    @JacocoGenerated
    @JsonCreator
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public IndexDocument(@JsonProperty("publicationType") String publicationType,
                         @JsonProperty("id") UUID id,
                         @JsonProperty("doi") URI doi,
                         @JsonProperty("contributors") List<IndexContributor> contributors,
                         @JsonProperty("title") String mainTitle,
                         @JsonProperty("abstract") String publicationAbstract,
                         @JsonProperty("description") String description,
                         @JsonProperty("owner") String owner,
                         @JsonProperty("publicationDate") IndexDate publicationDate,
                         @JsonProperty("publisher") IndexPublisher publisher,
                         @JsonProperty("modifiedDate") Instant modifiedDate,
                         @JsonProperty("publishedDate") Instant publishedDate,
                         @JsonProperty("alternativeTitles") Map<String, String> alternativeTitles,
                         @JsonProperty("tags") List<String> tags,
                         @JsonProperty("reference") Reference reference) {
        this.publicationType = publicationType;
        this.id = id;
        this.doi = doi;
        this.contributors = contributors;
        this.title = mainTitle;
        this.publicationDate = publicationDate;
        this.description = description;
        this.publicationAbstract = publicationAbstract;
        this.owner = owner;
        this.publisher = publisher;
        this.modifiedDate = modifiedDate;
        this.publishedDate = publishedDate;
        this.alternativeTitles = alternativeTitles;
        this.tags = tags;
        this.reference = reference;
    }

    protected IndexDocument(Builder builder) {
        publicationType = builder.publicationType;
        id = builder.id;
        doi = builder.doi;
        contributors = builder.contributors;
        title = builder.title;
        description = builder.description;
        owner = builder.owner;
        publicationDate = builder.publicationDate;
        publicationAbstract = builder.publicationAbstract;
        publisher = builder.publisher;
        modifiedDate = builder.modifiedDate;
        publishedDate = builder.publishedDate;
        alternativeTitles = builder.alternativeTitles;
        tags = builder.tags;
        reference = builder.reference;
    }

    @JacocoGenerated
    public String getPublicationType() {
        return publicationType;
    }

    @JacocoGenerated
    public UUID getId() {
        return id;
    }

    @JacocoGenerated
    public URI getDoi() {
        return doi;
    }

    @JacocoGenerated
    public List<IndexContributor> getContributors() {
        return contributors;
    }

    @JacocoGenerated
    public String getTitle() {
        return title;
    }

    @JacocoGenerated
    public IndexDate getPublicationDate() {
        return publicationDate;
    }

    @JacocoGenerated
    public String getAbstract() {
        return publicationAbstract;
    }

    @JacocoGenerated
    public String getDescription() {
        return description;
    }

    @JacocoGenerated
    public String getOwner() {
        return owner;
    }

    @JacocoGenerated
    public IndexPublisher getPublisher() {
        return publisher;
    }

    @JacocoGenerated
    public Instant getModifiedDate() {
        return modifiedDate;
    }

    @JacocoGenerated
    public Instant getPublishedDate() {
        return publishedDate;
    }

    @JacocoGenerated
    public Map<String, String> getAlternativeTitles() {
        return alternativeTitles;
    }

    @JacocoGenerated
    public List<String> getTags() {
        return tags;
    }

    @JacocoGenerated
    public Reference getReference() {
        return reference;
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
        return Objects.equals(publicationType, that.publicationType)
            && Objects.equals(id, that.id)
            && Objects.equals(doi, that.doi)
            && Objects.equals(contributors, that.contributors)
            && Objects.equals(title, that.title)
            && Objects.equals(owner, that.owner)
            && Objects.equals(description, that.description)
            && Objects.equals(publicationAbstract, that.publicationAbstract)
            && Objects.equals(publicationDate, that.publicationDate)
            && Objects.equals(publisher, that.publisher)
            && Objects.equals(modifiedDate, that.modifiedDate)
            && Objects.equals(publishedDate, that.publishedDate)
            && Objects.equals(alternativeTitles, that.alternativeTitles)
            && Objects.equals(tags, that.tags)
            && Objects.equals(reference, that.reference);
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(publicationType,
                id,
                doi,
                contributors,
                title,
                publicationDate,
                owner,
                description,
                publicationAbstract,
                publisher,
                modifiedDate,
                publishedDate,
                alternativeTitles,
                tags,
                reference);
    }

    @JacocoGenerated
    @Override
    public String toString() {
        return toJsonString();
    }

    public static final class Builder {

        private String publicationType;
        private UUID id;
        private URI doi;
        private List<IndexContributor> contributors;
        private IndexDate publicationDate;
        private String title;
        private String publicationAbstract;
        private String description;
        private String owner;
        private IndexPublisher publisher;
        private Instant modifiedDate;
        private Instant publishedDate;
        private Map<String, String> alternativeTitles;
        private List<String> tags;
        private Reference reference;

        public Builder() {
        }

        public Builder withType(String type) {
            this.publicationType = type;
            return this;
        }

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withDoi(URI doi) {
            this.doi = doi;
            return this;
        }

        public Builder withOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder withContributors(List<IndexContributor> contributors) {
            this.contributors = contributors;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withAbstract(String publicationAbstract) {
            this.publicationAbstract = publicationAbstract;
            return this;
        }

        @JacocoGenerated
        @SuppressWarnings("PMD.NullAssignment")
        public Builder withPublicationDate(IndexDate date) {
            this.publicationDate = isNonNullDate(date) ? date : null;
            return this;
        }

        public Builder withPublisher(IndexPublisher publisher) {
            this.publisher = publisher;
            return this;
        }

        public Builder withModifiedDate(Instant modifiedDate) {
            this.modifiedDate = modifiedDate;
            return this;
        }

        public Builder withPublishedDate(Instant publishedDate) {
            this.publishedDate = publishedDate;
            return this;
        }

        public Builder withAlternativeTitles(Map<String, String> alternativeTitles) {
            if (nonNull(alternativeTitles)) {
                this.alternativeTitles = Map.copyOf(alternativeTitles);
            } else {
                this.alternativeTitles = Collections.emptyMap();
            }
            return this;
        }

        public Builder withTags(List<String> tags) {
            if (nonNull(tags)) {
                this.tags = List.copyOf(tags);
            } else {
                this.tags = Collections.emptyList();
            }
            return this;
        }

        public Builder withReference(Reference reference) {
            if (nonNull(reference)) {
                this.reference = reference;
            }
            return this;
        }

        @JacocoGenerated
        private boolean isNonNullDate(IndexDate date) {
            return nonNull(date) && date.isPopulated();
        }

        public IndexDocument build() {
            return new IndexDocument(this);
        }
    }
}
