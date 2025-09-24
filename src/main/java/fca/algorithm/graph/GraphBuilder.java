package fca.algorithm.graph;

import fca.utils.concept.AEConcept_id;
import fca.utils.concept.Concept;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import static fca.utils.util.is_subset; // 导入用于判断真子集的函数

/**
 * 用于从概念格构建图结构的工具类。
 */
public class GraphBuilder {

    /**
     * 【已修改】通过朴素的O(n^3)算法，根据概念间的覆盖关系构造一个邻接矩阵 A_plus。
     * 矩阵的维度由指定的对象总数决定。
     * 如果概念 C_parent 是概念 C_child 的直接父概念（即覆盖关系），
     * 那么对于 C_parent 外延中的每个对象 x_i 和 C_child 外延中的每个对象 x_j，
     * 矩阵 A_plus[i-1][j-1] 的值会增加 1。
     *
     * @param concepts    形式概念的列表，构成了概念格。
     * @param objectCount 形式背景中的对象总数，用于定义邻接矩阵的维度。
     * @return 代表对象间连接强度的邻接矩阵 A_plus。
     */
    public static int[][] buildAdjacencyMatrix(ArrayList<Concept> concepts, int objectCount) {
        if (concepts == null || concepts.isEmpty() || objectCount <= 0) {
            return new int[0][0];
        }

        // --- 步骤 1: 使用传入的参数初始化邻接矩阵 ---
        int[][] a_plus = new int[objectCount][objectCount];

        // --- 步骤 2: 使用 O(n^3) 算法寻找覆盖关系并更新矩阵 ---
        int n = concepts.size();
        // 遍历每一对可能的 (子概念, 父概念) 组合
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    continue;
                }

                Concept c_child = concepts.get(i);
                Concept c_parent = concepts.get(j);

                // 条件1: c_child 必须是 c_parent 的严格子概念
                // 我们使用 is_subset 函数，它判断 c_parent.getExtent() 是否真包含 c_child.getExtent()
                if (is_subset(c_parent.getExtent(), c_child.getExtent())) {

                    boolean isDirectParent = true;
                    // 条件2: 检查是否存在任何“中间”概念 c_k
                    for (int k = 0; k < n; k++) {
                        if (k == i || k == j) {
                            continue;
                        }
                        Concept c_k = concepts.get(k);

                        // 如果存在 c_k 使得 c_child < c_k < c_parent
                        if (is_subset(c_k.getExtent(), c_child.getExtent()) && is_subset(c_parent.getExtent(), c_k.getExtent())) {
                            isDirectParent = false;
                            break; // 已找到中间概念，c_parent 不直接覆盖 c_child
                        }
                    }

                    // 如果检查完所有 c_k 后，仍然是直接父子关系
                    if (isDirectParent) {
                        // --- 步骤 3: 更新邻接矩阵 A_plus ---
                        BitSet parentExtent = c_parent.getExtent();
                        BitSet childExtent = c_child.getExtent();

                        // 遍历父概念外延中的每个对象 xi
                        for (int xi = parentExtent.nextSetBit(1); xi >= 0; xi = parentExtent.nextSetBit(xi + 1)) {
                            // 遍历子概念外延中的每个对象 xj
                            for (int xj = childExtent.nextSetBit(1); xj >= 0; xj = childExtent.nextSetBit(xj + 1)) {
                                // 对象ID从1开始，数组索引从0开始，所以需要减1
                                if (xi - 1 < objectCount && xj - 1 < objectCount) {
                                    a_plus[xi - 1][xj - 1]++;
                                }
                            }
                        }
                    }
                }
            }
        }

        return a_plus;
    }

    public static int[][] buildAEAdjacencyMatrix(ArrayList<AEConcept_id> concepts, int objectCount){
        if (concepts == null || concepts.isEmpty() || objectCount <= 0) {
            return new int[0][0];
        }

        // --- 步骤 1: 使用传入的参数初始化邻接矩阵 ---
        int[][] a_negative = new int[objectCount][objectCount];

        // --- 步骤 2: 使用 O(n^3) 算法寻找覆盖关系并更新矩阵 ---
        int n = concepts.size();
        // 遍历每一对可能的 (子概念, 父概念) 组合
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    continue;
                }

                AEConcept_id c_child = concepts.get(i);
                AEConcept_id c_parent = concepts.get(j);

                // 条件1: c_child 必须是 c_parent 的严格子概念
                // 我们使用 is_subset 函数，它判断 c_parent.getExtent_n() 是否真包含 c_child.getExtent_n()
                if (is_subset(c_parent.getExtent_n(), c_child.getExtent_n())) {

                    boolean isDirectParent = true;
                    // 条件2: 检查是否存在任何“中间”概念 c_k
                    for (int k = 0; k < n; k++) {
                        if (k == i || k == j) {
                            continue;
                        }
                        AEConcept_id c_k = concepts.get(k);

                        // 如果存在 c_k 使得 c_child < c_k < c_parent
                        if (is_subset(c_k.getExtent_n(), c_child.getExtent_n()) && is_subset(c_parent.getExtent_n(), c_k.getExtent_n())) {
                            isDirectParent = false;
                            break; // 已找到中间概念，c_parent 不直接覆盖 c_child
                        }
                    }

                    // 如果检查完所有 c_k 后，仍然是直接父子关系
                    if (isDirectParent) {
                        // --- 步骤 3: 更新邻接矩阵 A_negative ---
                        BitSet parentExtent = c_parent.getExtent_n();
                        BitSet childExtent = c_child.getExtent_n();

                        // 遍历父概念外延中的每个对象 xi
                        for (int xi = parentExtent.nextSetBit(1); xi >= 0; xi = parentExtent.nextSetBit(xi + 1)) {
                            // 遍历子概念外延中的每个对象 xj
                            for (int xj = childExtent.nextSetBit(1); xj >= 0; xj = childExtent.nextSetBit(xj + 1)) {
                                // 对象ID从1开始，数组索引从0开始，所以需要减1
                                if (xi - 1 < objectCount && xj - 1 < objectCount) {
                                    a_negative[xi - 1][xj - 1]++;
                                }
                            }
                        }
                    }
                }
            }
        }

        return a_negative;
    }

    /**
     * 【新方法】将一个整数矩阵写入到指定路径的文件中，格式为 CSV。
     *
     * @param matrix 要写入的二维整数数组。
     * @param filePath 输出文件的完整路径 (例如, "src/main/java/data/graph/A_plus.csv")。
     * @throws IOException 如果文件写入时发生错误。
     */
    public static void writeMatrixToFile(int[][] matrix, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    writer.write(Integer.toString(matrix[i][j]));
                    // 如果不是当前行的最后一个元素，则添加逗号
                    if (j < matrix[i].length - 1) {
                        writer.write(",");
                    }
                }
                // 每写完一行后换行
                writer.newLine();
            }
        }
        System.out.println("邻接矩阵已成功写入到: " + filePath);
    }
}