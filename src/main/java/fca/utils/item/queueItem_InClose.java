package fca.utils.item;

import java.util.BitSet;

public class queueItem_InClose {
    private BitSet W;
    private int j;

    public queueItem_InClose(BitSet w, int j) {
        W = w;
        this.j = j;
    }

    public BitSet getW() {
        return W;
    }

    public int getJ() {
        return j;
    }
}
