package fca.utils.concept;

import java.util.BitSet;

public class OEConcept {
    private BitSet extent;
    private BitSet intent;
    private BitSet intent_n;

    @Override
    public String toString() {
        return "OEConcept{" +
                "extent=" + extent +
                ", intent=" + intent +
                ", intent_n=" + intent_n +
                '}';
    }

    public BitSet getExtent() {
        return extent;
    }

    public void setExtent(BitSet extent) {
        this.extent = extent;
    }

    public BitSet getIntent() {
        return intent;
    }

    public void setIntent(BitSet intent) {
        this.intent = intent;
    }

    public BitSet getIntent_n() {
        return intent_n;
    }

    public void setIntent_n(BitSet intent_n) {
        this.intent_n = intent_n;
    }

    public OEConcept() {
    }

    public OEConcept(BitSet extent, BitSet intent, BitSet intent_n) {
        this.extent = extent;
        this.intent = intent;
        this.intent_n = intent_n;
    }
}
