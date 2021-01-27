package no.unit.bibs.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nva.commons.utils.JacocoGenerated;

import java.net.URI;
import java.util.Objects;

public class IndexPublisher {
    private final URI id;
    private final String name;

    @JsonCreator
    public IndexPublisher(@JsonProperty("id") URI id,
                          @JsonProperty("name") String name) {
        this.id = id;
        this.name = name;
    }

    private IndexPublisher(Builder builder) {
        id = builder.id;
        name = builder.name;
    }

    public URI getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static final class Builder {
        private URI id;
        private String name;

        public Builder() {
        }

        public Builder withId(URI id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public IndexPublisher build() {
            return new IndexPublisher(this);
        }
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IndexPublisher)) {
            return false;
        }
        IndexPublisher that = (IndexPublisher) o;
        return Objects.equals(getId(), that.getId())
                && Objects.equals(getName(), that.getName());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }
}
