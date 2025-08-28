package fca.utils.item;

import java.util.BitSet;

public class oeQueueItem {
    private BitSet intent;
    private BitSet intent_n;
    private int j;

    public BitSet getIntent() {
        return intent;
    }

    public BitSet getIntent_n() {
        return intent_n;
    }

    public int getJ() {
        return j;
    }

    public oeQueueItem(BitSet intent, BitSet intent_n, int j) {
        this.intent = intent;
        this.intent_n = intent_n;
        this.j = j;
    }
}
