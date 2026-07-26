package fca.algorithm.concept3c_ae;

import fca.utils.Context;
import fca.utils.concept.AEConcept;
import fca.utils.item.aeQueueItem;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.Queue;

import static fca.utils.util.*;

/**
 * 冰山格版核心三支 AE 概念构造算法（基于 ICbo53C_ae, core 语义: 正负外延均非空）。
 *
 * 额外提供两个支持度阈值：
 *   minSupPos —— 正外延最小基数 |extent|   >= minSupPos
 *   minSupNeg —— 负外延最小基数 |extent_n| >= minSupNeg
 * 阈值取 1 时与原 core 算法结果一致（非空即基数>=1）。
 *
 * 剪枝正确性：在 CbO 系枚举中，子概念在属性上只增不减，因此
 * extent 与 extent_n 沿生成树均单调收缩；候选 (new_extent, new_extent_n)
 * 任一低于阈值时，其整棵子树中所有概念的对应外延都不会更大，可整体剪掉，
 * 处理方式与原算法中"外延为空"完全相同（Q.set(j) 屏蔽该属性）。
 * 而任何满足阈值的概念，其规范生成路径上的祖先外延只会更大，不会被误剪。
 */
public class ICbo53C_aeIceberg {
    public static void exe(Context context, AEConcept aeconcept, int v, BitSet P,
                           Queue<AEConcept> res, int minSupPos, int minSupNeg) {
        Queue<aeQueueItem> queue = new LinkedList<>();
        int attrs_size = context.getAttrs_size();
        BitSet intent = aeconcept.getIntent();
        BitSet extent = aeconcept.getExtent();
        BitSet extent_n = aeconcept.getExtent_n();
        BitSet Q = (BitSet) P.clone();
        for (int j = v; j <= attrs_size; j++) {
            if (!intent.get(j) && !Q.get(j)) {
                BitSet new_extent = intersection(extent, context.getAttrs().get(j));
                BitSet new_extent_n = intersection(extent_n, context.getAttrs_n().get(j));
                boolean okPos = (minSupPos <= 1) ? !new_extent.isEmpty() : new_extent.cardinality() >= minSupPos;
                boolean okNeg = (minSupNeg <= 1) ? !new_extent_n.isEmpty() : new_extent_n.cardinality() >= minSupNeg;
                if (okPos && okNeg) {
                    if (extent.equals(new_extent) && extent_n.equals(new_extent_n)) {
                        intent.set(j);
                    } else {
                        BitSet W_j = intersection(get_objs_shared(context, new_extent), get_vj(j));
                        BitSet Z_j = intersection(get_objs_not_shared(context, new_extent_n), get_vj(j));
                        BitSet M_j = intersection(W_j, Z_j);
                        BitSet I = intersection(intent, get_vj(j));
                        if (I.equals(M_j)) {
                            queue.offer(new aeQueueItem(new_extent, new_extent_n, j));
                        } else {
                            if (difference(M_j, I).nextSetBit(0) < v) {
                                Q.set(j);
                            }
                        }
                    }
                } else {
                    Q.set(j);
                }
            }
        }
        res.offer(aeconcept);
        while (!queue.isEmpty()) {
            aeQueueItem item = queue.poll();
            BitSet new_intent = (BitSet) intent.clone();
            new_intent.set(item.getJ());
            AEConcept new_aeConcept = new AEConcept(item.getExtent(), item.getExtent_n(), new_intent);
            exe(context, new_aeConcept, item.getJ() + 1, Q, res, minSupPos, minSupNeg);
        }
    }
}
