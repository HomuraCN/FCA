package fca.algorithm.concept;

import fca.utils.Context;
import fca.utils.concept.Concept;
import fca.utils.item.queueItem_InClose;

import java.util.*;

import static fca.utils.util.*;

/**
 * 冰山概念格 (Iceberg Concept Lattice) 版 InClose3。
 *
 * 只枚举外延基数 |extent| >= minSupport 的概念（即支持度不低于阈值的频繁闭项集，
 * Stumme 等人 TITANIC 一文中的 iceberg lattice）。
 *
 * 剪枝正确性：在 CbO/InClose 系算法中，子概念外延恒为父概念外延的子集，
 * 因此支持度沿生成树单调不增。某属性 j 产生的候选外延 W=extent∩{j}' 若
 * |W| < minSupport，则以其为根的整棵子树中所有概念支持度均 < minSupport，
 * 可以整体剪掉；而任何频繁概念的规范生成路径只经过频繁概念，不会因此丢失。
 *
 * minSupport <= 0 时行为与原 InClose3 完全一致。
 */
public class InClose3Iceberg {
    public static void inClose3Iceberg_exe(Context context, Concept concept, int v,
                                           Map<Integer, BitSet> nj, Queue<Concept> res,
                                           int minSupport, int[] idCounter) {
        Queue<queueItem_InClose> queue = new LinkedList<>();
        Map<Integer, BitSet> mj = new HashMap<>();
        int attr_size = context.getAttrs_size();
        for (int j = v; j <= attr_size; j++) {
            BitSet intent = concept.getIntent();
            BitSet extent = concept.getExtent();
            mj.put(j, nj.get(j));
            if ((intent.isEmpty() || !intent.get(j)) && is_subset_eq(intersection(intent, get_vj(j)), nj.get(j))) {
                BitSet W = intersection(extent, context.getAttrs().get(j));
                // --- iceberg 剪枝：候选外延不频繁，整棵子树剪掉 ---
                if (minSupport > 0 && W.cardinality() < minSupport) {
                    continue;
                }
                if (extent.equals(W)) {
                    intent.set(j);
                    concept.setIntent(intent);
                } else {
                    BitSet A_Vj = intersection(intent, get_vj(j));
                    BitSet W_j = intersection(get_objs_shared(context, W), get_vj(j));
                    if (A_Vj.equals(W_j)) {
                        queue.offer(new queueItem_InClose(W, j));
                    } else {
                        mj.put(j, W_j);
                    }
                }
            }
        }
        Concept concept_temp = new Concept(concept.getExtent(), concept.getIntent(), idCounter[0]++);
        res.offer(concept_temp);

        while (!queue.isEmpty()) {
            queueItem_InClose item = queue.poll();
            BitSet B = (BitSet) concept.getIntent().clone();
            B.set(item.getJ());
            Concept newConcept = new Concept(item.getW(), B);
            inClose3Iceberg_exe(context, newConcept, item.getJ() + 1, mj, res, minSupport, idCounter);
        }
    }
}
