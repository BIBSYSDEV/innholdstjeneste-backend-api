package no.unit.bibs.elasticsearch;

import java.net.URI;

/**
 * Class provided to simplify creation of objects for testing. Clones some functionality from nva-datamodel-java.
 */
public class Contributor {

    private final Integer sequence;
    private final String name;
    private final String arpId;
    private final URI id;

    /**
     * Constructor for Contributor testing helper object.
     * @param sequence The number in a list that the contributor should appear.
     * @param name The name of the contributor.
     * @param arpId The ARP identifier of the contributor.
     * @param id The ARP id (URI) of the contributor.
     */
    public Contributor(Integer sequence, String name, String arpId, URI id) {
        this.sequence = sequence;
        this.name = name;
        this.arpId = arpId;
        this.id = id;
    }

    public Integer getSequence() {
        return sequence;
    }

    public String getName() {
        return name;
    }

    public String getArpId() {
        return arpId;
    }

    public URI getId() {
        return id;
    }

    /**
     * Creates an IndexContributor representation of the Contributor.
     * @return IndexContributor.
     */
    public IndexContributor toIndexContributor() {
        return new IndexContributor.Builder()
                .withName(name)
                .withId(id)
                .build();

    }
}
