package fca.algorithm.encoding;

import fca.utils.concept.AEConcept_id;
import fca.utils.concept.Concept;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static fca.utils.util.is_subset;

/**
 * 从概念格中计算对象级概念位置编码（Conceptual Positional Encoding, CPE）。
 *
 * <p>概念深度定义为从任一顶层概念到当前概念的最长特化链长度；
 * 之后再通过概念外延，将概念级深度和广度聚合回对象节点。</p>
 */
public class ConceptPositionEncoder {

    public static final int CPE_DIMENSION = 2;
    public static final int DEFAULT_DEPTH_PROFILE_BINS = 8;

    public enum DepthAggregationMode {
        MEAN,
        MAX,
        SUPPORT_WEIGHTED_MEAN
    }

    private ConceptPositionEncoder() {
    }

    /**
     * 使用普通形式概念的外延计算概念深度。
     */
    public static int[] computeConceptDepths(ArrayList<Concept> concepts) {
        return computeConceptDepths(concepts, Concept::getExtent);
    }

    /**
     * 使用属性导出三支概念的负外延 extent_n 计算概念深度。
     *
     * <p>这里与负概念格图 A_negative 的构造保持一致。</p>
     */
    public static int[] computeNegativeConceptDepths(ArrayList<AEConcept_id> concepts) {
        return computeConceptDepths(concepts, AEConcept_id::getExtent_n);
    }

    /**
     * 为普通概念格构造归一化后的对象级 CPE。
     *
     * <p>第 0 列：包含该对象的所有概念的平均深度，再按最大深度归一化。
     * 第 1 列：特异性加权概念广度，即越具体（外延越小）的概念贡献越大。</p>
     */
    public static double[][] computeObjectCPE(ArrayList<Concept> concepts, int objectCount) {
        return computeObjectCPE(concepts, objectCount, DepthAggregationMode.MEAN);
    }

    public static double[][] computeObjectCPE(ArrayList<Concept> concepts,
                                              int objectCount,
                                              DepthAggregationMode depthMode) {
        int[] conceptDepths = computeConceptDepths(concepts);
        return aggregateObjectCPE(concepts, Concept::getExtent, conceptDepths, objectCount, depthMode);
    }

    /**
     * 为三支概念格的负视图构造归一化后的对象级 CPE。
     *
     * <p>对象所属关系通过 extent_n 判断，因此该编码应与 A_negative 分支配套使用。</p>
     */
    public static double[][] computeNegativeObjectCPE(ArrayList<AEConcept_id> concepts, int objectCount) {
        return computeNegativeObjectCPE(concepts, objectCount, DepthAggregationMode.MEAN);
    }

    public static double[][] computeNegativeObjectCPE(ArrayList<AEConcept_id> concepts,
                                                      int objectCount,
                                                      DepthAggregationMode depthMode) {
        int[] conceptDepths = computeNegativeConceptDepths(concepts);
        return aggregateObjectCPE(concepts, AEConcept_id::getExtent_n, conceptDepths, objectCount, depthMode);
    }

    /**
     * 为普通概念格构造 depth profile 形式的对象级 CPE。
     *
     * <p>前 binCount 列表示对象所属概念在不同深度区间上的特异性加权分布；
     * 最后一列表示特异性加权概念广度。</p>
     */
    public static double[][] computeObjectDepthProfileCPE(ArrayList<Concept> concepts,
                                                          int objectCount,
                                                          int binCount) {
        int[] conceptDepths = computeConceptDepths(concepts);
        return aggregateObjectDepthProfileCPE(concepts, Concept::getExtent, conceptDepths, objectCount, binCount);
    }

    /**
     * 为三支概念格负视图构造 depth profile 形式的对象级 CPE。
     *
     * <p>对象所属关系通过 extent_n 判断，因此该编码应与 A_negative 分支配套使用。</p>
     */
    public static double[][] computeNegativeObjectDepthProfileCPE(ArrayList<AEConcept_id> concepts,
                                                                  int objectCount,
                                                                  int binCount) {
        int[] conceptDepths = computeNegativeConceptDepths(concepts);
        return aggregateObjectDepthProfileCPE(concepts, AEConcept_id::getExtent_n, conceptDepths, objectCount, binCount);
    }

    public static void writeCPEToFile(double[][] cpe, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (double[] row : cpe) {
                for (int i = 0; i < row.length; i++) {
                    writer.write(Double.toString(row[i]));
                    if (i < row.length - 1) {
                        writer.write(",");
                    }
                }
                writer.newLine();
            }
        }
        System.out.println("CPE 矩阵已成功写入到: " + filePath);
    }

    private static <T> int[] computeConceptDepths(List<T> concepts, Function<T, BitSet> extentGetter) {
        int n = concepts == null ? 0 : concepts.size();
        int[] depths = new int[n];
        if (n == 0) {
            return depths;
        }

        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            order.add(i);
        }

        order.sort(Comparator
                .comparingInt((Integer i) -> safeExtent(extentGetter.apply(concepts.get(i))).cardinality())
                .reversed());

        for (int childPos = 0; childPos < order.size(); childPos++) {
            int childIndex = order.get(childPos);
            BitSet childExtent = safeExtent(extentGetter.apply(concepts.get(childIndex)));

            for (int parentPos = 0; parentPos < childPos; parentPos++) {
                int parentIndex = order.get(parentPos);
                BitSet parentExtent = safeExtent(extentGetter.apply(concepts.get(parentIndex)));

                if (parentExtent.cardinality() <= childExtent.cardinality()) {
                    continue;
                }
                if (is_subset(parentExtent, childExtent)) {
                    depths[childIndex] = Math.max(depths[childIndex], depths[parentIndex] + 1);
                }
            }
        }

        return depths;
    }

    private static <T> double[][] aggregateObjectCPE(List<T> concepts,
                                                     Function<T, BitSet> extentGetter,
                                                     int[] conceptDepths,
                                                     int objectCount,
                                                     DepthAggregationMode depthMode) {
        double[][] cpe = new double[Math.max(objectCount, 0)][CPE_DIMENSION];
        if (concepts == null || concepts.isEmpty() || objectCount <= 0) {
            return cpe;
        }

        double[] depthSums = new double[objectCount];
        double[] weightedDepthSums = new double[objectCount];
        double[] depthWeightSums = new double[objectCount];
        int[] maxDepthsByObject = new int[objectCount];
        double[] breadthSums = new double[objectCount];
        int[] conceptCounts = new int[objectCount];
        int maxDepth = 0;

        for (int depth : conceptDepths) {
            if (depth > maxDepth) {
                maxDepth = depth;
            }
        }

        for (int conceptIndex = 0; conceptIndex < concepts.size(); conceptIndex++) {
            BitSet extent = safeExtent(extentGetter.apply(concepts.get(conceptIndex)));
            int depth = conceptDepths[conceptIndex];
            double breadthWeight = getSpecificityWeight(extent);

            for (int objectId = extent.nextSetBit(1);
                 objectId >= 0 && objectId <= objectCount;
                 objectId = extent.nextSetBit(objectId + 1)) {
                int rowIndex = objectId - 1;
                depthSums[rowIndex] += depth;
                weightedDepthSums[rowIndex] += depth * breadthWeight;
                depthWeightSums[rowIndex] += breadthWeight;
                maxDepthsByObject[rowIndex] = Math.max(maxDepthsByObject[rowIndex], depth);
                breadthSums[rowIndex] += breadthWeight;
                conceptCounts[rowIndex]++;
            }
        }

        double minBreadth = Double.POSITIVE_INFINITY;
        double maxBreadth = Double.NEGATIVE_INFINITY;
        for (double breadth : breadthSums) {
            if (breadth < minBreadth) {
                minBreadth = breadth;
            }
            if (breadth > maxBreadth) {
                maxBreadth = breadth;
            }
        }

        for (int i = 0; i < objectCount; i++) {
            double objectDepth = switch (depthMode) {
                case MAX -> maxDepthsByObject[i];
                case SUPPORT_WEIGHTED_MEAN -> depthWeightSums[i] > 0
                        ? weightedDepthSums[i] / depthWeightSums[i]
                        : 0.0;
                case MEAN -> conceptCounts[i] > 0 ? depthSums[i] / conceptCounts[i] : 0.0;
            };
            double normalizedDepth = maxDepth > 0 ? objectDepth / maxDepth : 0.0;
            double normalizedBreadth = maxBreadth > minBreadth
                    ? (breadthSums[i] - minBreadth) / (maxBreadth - minBreadth)
                    : 0.0;

            cpe[i][0] = normalizedDepth;
            cpe[i][1] = normalizedBreadth;
        }

        return cpe;
    }

    private static double getSpecificityWeight(BitSet extent) {
        int support = safeExtent(extent).cardinality();
        return support > 0 ? 1.0 / Math.log1p(support) : 0.0;
    }

    private static <T> double[][] aggregateObjectDepthProfileCPE(List<T> concepts,
                                                                 Function<T, BitSet> extentGetter,
                                                                 int[] conceptDepths,
                                                                 int objectCount,
                                                                 int binCount) {
        int bins = Math.max(binCount, 1);
        double[][] cpe = new double[Math.max(objectCount, 0)][bins + 1];
        if (concepts == null || concepts.isEmpty() || objectCount <= 0) {
            return cpe;
        }

        int maxDepth = 0;
        for (int depth : conceptDepths) {
            if (depth > maxDepth) {
                maxDepth = depth;
            }
        }

        double[][] profileSums = new double[objectCount][bins];
        double[] breadthSums = new double[objectCount];

        for (int conceptIndex = 0; conceptIndex < concepts.size(); conceptIndex++) {
            BitSet extent = safeExtent(extentGetter.apply(concepts.get(conceptIndex)));
            double breadthWeight = getSpecificityWeight(extent);
            int binIndex = getDepthBin(conceptDepths[conceptIndex], maxDepth, bins);

            for (int objectId = extent.nextSetBit(1);
                 objectId >= 0 && objectId <= objectCount;
                 objectId = extent.nextSetBit(objectId + 1)) {
                int rowIndex = objectId - 1;
                profileSums[rowIndex][binIndex] += breadthWeight;
                breadthSums[rowIndex] += breadthWeight;
            }
        }

        double minBreadth = Double.POSITIVE_INFINITY;
        double maxBreadth = Double.NEGATIVE_INFINITY;
        for (double breadth : breadthSums) {
            if (breadth < minBreadth) {
                minBreadth = breadth;
            }
            if (breadth > maxBreadth) {
                maxBreadth = breadth;
            }
        }

        for (int objectIndex = 0; objectIndex < objectCount; objectIndex++) {
            if (breadthSums[objectIndex] > 0) {
                for (int binIndex = 0; binIndex < bins; binIndex++) {
                    cpe[objectIndex][binIndex] = profileSums[objectIndex][binIndex] / breadthSums[objectIndex];
                }
            }
            cpe[objectIndex][bins] = maxBreadth > minBreadth
                    ? (breadthSums[objectIndex] - minBreadth) / (maxBreadth - minBreadth)
                    : 0.0;
        }

        return cpe;
    }

    private static int getDepthBin(int depth, int maxDepth, int binCount) {
        if (maxDepth <= 0) {
            return 0;
        }
        int binIndex = (int) Math.floor(((double) depth / maxDepth) * binCount);
        return Math.min(binIndex, binCount - 1);
    }

    private static BitSet safeExtent(BitSet extent) {
        return extent == null ? new BitSet() : extent;
    }
}
