package no.unit.bibs.contents;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nva.commons.core.JacocoGenerated;

import java.util.Objects;

public class ContentsRequest {

    private final ContentsDocument contents;

    @JsonCreator
    public ContentsRequest(@JsonProperty("contents") ContentsDocument contents) {
        this.contents = contents;
    }

    protected ContentsRequest(Builder builder) {
        this.contents = builder.contents;
    }

    public ContentsDocument getContents() {
        return contents;
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        return this == o
               || (o instanceof ContentsRequest && Objects.equals(contents, ((ContentsRequest) o).contents));
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(contents);
    }

    @JacocoGenerated
    public static final class Builder {

        private ContentsDocument contents;

        public ContentsRequest.Builder withContents(ContentsDocument contents) {
            this.contents = contents;
            return this;
        }

        public ContentsRequest build() {
            return new ContentsRequest(this);
        }

    }

}
