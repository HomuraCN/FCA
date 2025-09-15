package fca.utils.item;

import fca.utils.concept.OEConcept;

import java.util.BitSet;
import java.util.Map;

public class oeConceptItem {
    private OEConcept oeConcept;
    private int j;
    private Map<Integer, BitSet> nj;

    public OEConcept getOeConcept() {
        return oeConcept;
    }

    public int getJ() {
        return j;
    }

    public Map<Integer, BitSet> getNj() {
        return nj;
    }

    public oeConceptItem(OEConcept oeConcept, int j, Map<Integer, BitSet> nj) {
        this.oeConcept = oeConcept;
        this.j = j;
        this.nj = nj;
    }
}
