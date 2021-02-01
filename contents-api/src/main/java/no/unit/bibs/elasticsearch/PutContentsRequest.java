package no.unit.bibs.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nva.commons.utils.JacocoGenerated;

import java.util.Objects;

public class PutContentsRequest {

    private final String contents;

    @JsonCreator
    public PutContentsRequest(@JsonProperty("contents") String contents) {
        this.contents = contents;
    }

    protected PutContentsRequest(Builder builder) {
        this.contents = builder.contents;
    }

    public String getContents() {
        return contents;
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (!(o instanceof PutContentsRequest)) {
            return false;
        }
        PutContentsRequest that = (PutContentsRequest) o;
        return Objects.equals(contents, that.contents);
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(contents);
    }

    @JacocoGenerated
    public static final class Builder {

        private String contents;

        public Builder() {
        }

        public PutContentsRequest.Builder withContents(String contents) {
            this.contents = contents;
            return this;
        }

        public PutContentsRequest build() {
            return new PutContentsRequest(this);
        }

    }

}
