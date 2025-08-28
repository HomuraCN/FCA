package fca.utils.item;

import fca.utils.concept.AEConcept;

import java.util.BitSet;
import java.util.Map;

public class aeConceptItem {
    private AEConcept aeConcept;
    private int j;
    private Map<Integer, BitSet> nj;
    private BitSet P;

    public aeConceptItem(AEConcept aeConcept, int j, Map<Integer, BitSet> nj) {
        this.aeConcept = aeConcept;
        this.j = j;
        this.nj = nj;
    }

    public aeConceptItem(AEConcept aeConcept, int j,BitSet P) {
        this.aeConcept = aeConcept;
        this.j = j;
        this.P = P;
    }

    public AEConcept getAeConcept() {
        return aeConcept;
    }

    public int getJ() {
        return j;
    }

    public Map<Integer, BitSet> getNj() {
        return nj;
    }

    public BitSet getP() {
        return P;
    }

    @Override
    public String toString() {
        return "aeConceptItem{" +
                "aeConcept=" + aeConcept +
                ", j=" + j +
                ", nj=" + nj +
                ", P=" + P +
                '}';
    }
}
