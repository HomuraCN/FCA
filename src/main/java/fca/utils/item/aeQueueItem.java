package fca.utils.item;

import java.util.BitSet;

public class aeQueueItem {
    private BitSet extent;
    private BitSet extent_n;
    private int j;

    public aeQueueItem(BitSet extent, BitSet extent_n, int j) {
        this.extent = extent;
        this.extent_n = extent_n;
        this.j = j;
    }

    public aeQueueItem() {
    }

    public BitSet getExtent() {
        return extent;
    }

    public BitSet getExtent_n() {
        return extent_n;
    }

    public int getJ() {
        return j;
    }
}
