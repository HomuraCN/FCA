package fca.utils.concept;

import java.util.BitSet;
import java.util.Objects;

public class Concept {
    private BitSet extent;
    private BitSet intent;
    private int id=0;

    public Concept() {
    }

    @Override
    public String toString() {
        return "Concept{" +
                "extent=" + extent +
                ", intent=" + intent +
                ", id=" + id +
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Concept(BitSet extent, BitSet intent) {
        this.extent = extent;
        this.intent = intent;
    }

    public Concept(BitSet extent, BitSet intent, int id) {
        this.extent = extent;
        this.intent = intent;
        this.id = id;
    }

    /**
     * 重写 equals 方法，定义相等的逻辑
     * 只要 extent 和 intent 相同，就认为两个 Concept 对象相等
     */
    @Override
    public boolean equals(Object o) {
        // 1. 检查是否是同一个对象的引用
        if (this == o) return true;
        // 2. 检查对象是否为 null，或者类型是否不匹配
        if (o == null || getClass() != o.getClass()) return false;
        // 3. 将对象转换为 Concept 类型
        Concept concept = (Concept) o;
        // 4. 比较核心字段 extent 和 intent 是否相等
        return Objects.equals(extent, concept.extent) &&
                Objects.equals(intent, concept.intent);
    }

    /**
     * 重写 hashCode 方法，为对象生成哈希码
     * 必须使用与 equals() 中相同的字段来计算
     */
    @Override
    public int hashCode() {
        // 使用 Objects.hash() 辅助方法可以方便地根据多个字段生成一个哈希码
        return Objects.hash(extent, intent);
    }


}
