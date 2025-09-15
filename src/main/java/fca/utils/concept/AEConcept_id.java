package fca.utils.concept;

import java.util.BitSet;

public class AEConcept_id {
    private int id;
    private BitSet extent;
    private BitSet extent_n;
    private BitSet intent;

    public AEConcept_id() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BitSet getExtent() {
        return extent;
    }

    public void setExtent(BitSet extent) {
        this.extent = extent;
    }

    public BitSet getExtent_n() {
        return extent_n;
    }

    public void setExtent_n(BitSet extent_n) {
        this.extent_n = extent_n;
    }

    public BitSet getIntent() {
        return intent;
    }

    public void setIntent(BitSet intent) {
        this.intent = intent;
    }

    @Override
    public String toString() {
        return "AEConcept_R{" +
                "id=" + id +
                ", extent=" + extent +
                ", extent_n=" + extent_n +
                ", intent=" + intent +
                '}';
    }

    public AEConcept_id(int id, BitSet extent, BitSet extent_n, BitSet intent) {
        this.id = id;
        this.extent = extent;
        this.extent_n = extent_n;
        this.intent = intent;
    }
}
