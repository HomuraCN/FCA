package fca.analysis;

import fca.utils.Context;
import fca.utils.concept.Concept;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class Analysis {
    /**
     * 计算信息还原度 (IRD)
     * @param originalContext 原始的形式背景 (I)
     * @param reverseContext 逆生成的形式背景，其属性为新属性 (B_i)。
     * 此处的BitSet被假定为1-based索引，以匹配文件读取格式。
     * @param reductionPermutation 一个BitSet，其置为1的位代表了组合约简使用了原概念格中的哪些概念
     * @param originalLatticeConcepts 原始概念格中的所有概念
     * @return IRD 计算结果 (一个0到1之间的double值)
     */
    public static double calculateIRD(Context originalContext,
                                      Context reverseContext,
                                      BitSet reductionPermutation,
                                      ArrayList<Concept> originalLatticeConcepts) {

        // 步骤 1: 创建从新属性到旧属性BitSet的映射
        // 新属性的顺序由reductionPermutation决定，此映射列表是0-indexed的
        ArrayList<BitSet> newAttrToOriginalAttrMap = new ArrayList<>();
        for (int i = reductionPermutation.nextSetBit(0); i >= 0; i = reductionPermutation.nextSetBit(i + 1)) {
            // 概念的ID从1开始，而ArrayList的索引从0开始
            if (i > 0 && i <= originalLatticeConcepts.size()) {
                Concept concept = originalLatticeConcepts.get(i - 1);
                newAttrToOriginalAttrMap.add(concept.getIntent());
            }
        }

        // 步骤 2: 将逆生成背景 I' 的新属性转换回原始属性
        Map<Integer, BitSet> reconstructedContextIPrime = new HashMap<>();
        int originalAttrSize = originalContext.getAttrs_size();

        Map<Integer, BitSet> reverseContextWithNewAttrs = reverseContext.getObjs();

        for (Map.Entry<Integer, BitSet> entry : reverseContextWithNewAttrs.entrySet()) {
            Integer objectId = entry.getKey();
            BitSet newAttrsOfObject1Based = entry.getValue(); // 明确变量为1-based
            BitSet originalAttrsForObject = new BitSet(originalAttrSize);

            // 遍历该对象拥有的每一个新属性 (j 是一个 1-based 的索引)
            for (int j = newAttrsOfObject1Based.nextSetBit(0); j >= 0; j = newAttrsOfObject1Based.nextSetBit(j + 1)) {
                // *** 核心改动 ***
                // 将1-based的属性索引j转换为0-based的ArrayList索引
                int attrIndex0Based = j - 1;

                // 检查转换后的0-based索引是否在有效范围内
                if (attrIndex0Based >= 0 && attrIndex0Based < newAttrToOriginalAttrMap.size()) {
                    // 使用0-based索引从映射列表中获取正确的原始属性BitSet
                    originalAttrsForObject.or(newAttrToOriginalAttrMap.get(attrIndex0Based));
                }
            }
            reconstructedContextIPrime.put(objectId, originalAttrsForObject);
        }

        // 步骤 3: 计算 |I| (原始背景中的关系总数)
        long totalRelationsI = 0;
        Map<Integer, BitSet> originalObjs = originalContext.getObjs();
        for (BitSet attrs : originalObjs.values()) {
            totalRelationsI += attrs.cardinality();
        }

        // 如果原始关系为0，则还原度为1（或未定义，这里返回1）
        if (totalRelationsI == 0) {
            return 1.0;
        }

        // 步骤 4: 计算 |I ∩ I'| (交集的大小)
        long intersectionSize = 0;
        for (int i = 1; i <= originalContext.getObjs_size(); i++) {
            BitSet originalAttrs = originalObjs.getOrDefault(i, new BitSet());
            BitSet reconstructedAttrs = reconstructedContextIPrime.getOrDefault(i, new BitSet());

            // 克隆一份并计算交集
            BitSet intersection = (BitSet) originalAttrs.clone();
            intersection.and(reconstructedAttrs);
            intersectionSize += intersection.cardinality();
        }

        // 步骤 5: 计算并返回IRD
        return (double) intersectionSize / totalRelationsI;
    }

    /**
     * 计算节点减少率 (Node Reduction Rate)
     * 公式: 1 - (|逆生成概念格节点| / |原概念格节点|)
     * @param originalLattice 原始概念格的所有概念列表
     * @param reverseLattice 组合约简，代表了逆生成概念格的所有节点
     * @return 节点减少率 (一个0到1之间的double值)
     */
    public static double calculateNodeReductionRate(ArrayList<Concept> originalLattice,
                                                    ArrayList<Concept> reverseLattice) {
        double originalNodeCount = originalLattice.size();
        double reducedNodeCount = reverseLattice.size();

        // 防止除以零的错误
        if (originalNodeCount == 0) {
            // 如果原概念格没有节点，可以认为减少率为0，或者根据业务场景返回NaN或抛出异常
            return 0.0;
        }

        return 1.0 - (reducedNodeCount / originalNodeCount);
    }
}