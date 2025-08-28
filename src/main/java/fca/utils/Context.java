package fca.utils;


import java.util.BitSet;
import java.util.Map;

public class Context {
    // 每个对象 拥有 的属性
    private Map<Integer, BitSet> objs;
    private Map<Integer, BitSet> attrs;
    // 每个对象 缺失 的属性
    private Map<Integer, BitSet> objs_n;
    private Map<Integer, BitSet> attrs_n;
    private int objs_size;
    private int attrs_size;

    public Map<Integer, BitSet> getObjs() {
        return objs;
    }

    public void setObjs(Map<Integer, BitSet> objs) {
        this.objs = objs;
    }

    public Map<Integer, BitSet> getAttrs() {
        return attrs;
    }

    public void setAttrs(Map<Integer, BitSet> attrs) {
        this.attrs = attrs;
    }

    public Map<Integer, BitSet> getObjs_n() {
        return objs_n;
    }

    public void setObjs_n(Map<Integer, BitSet> objs_n) {
        this.objs_n = objs_n;
    }

    public Map<Integer, BitSet> getAttrs_n() {
        return attrs_n;
    }

    public void setAttrs_n(Map<Integer, BitSet> attrs_n) {
        this.attrs_n = attrs_n;
    }

    public int getObjs_size() {
        return objs_size;
    }

    public void setObjs_size(int objs_size) {
        this.objs_size = objs_size;
    }

    public int getAttrs_size() {
        return attrs_size;
    }

    public void setAttrs_size(int attrs_size) {
        this.attrs_size = attrs_size;
    }

    public Context() {
    }
}



