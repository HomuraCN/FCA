package fca.algorithm.reverse;

import fca.utils.Context;
import fca.utils.concept.Concept;
import fca.utils.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ReverseGeneratedConcept {
    // 组合约简
    public ArrayList<Concept> combinationReduction(ArrayList<Concept> concepts, ArrayList<BitSet> reductions){
        if (reductions == null || reductions.isEmpty()) {
            return new ArrayList<>();
        }

        BitSet minBitSet = null;
        int minCardinality = Integer.MAX_VALUE;

        // 找到基数最小的BitSet
        for (BitSet bs : reductions) {
            int currentCardinality = bs.cardinality();
            if (currentCardinality < minCardinality) {
                minCardinality = currentCardinality;
                minBitSet = bs;
            }
        }

        ArrayList<Concept> combinationReduction = new ArrayList<>();
        if (minBitSet != null) {
            // 遍历minBitSet中所有为1的位
            for (int i = minBitSet.nextSetBit(0); i >= 0; i = minBitSet.nextSetBit(i + 1)) {
                // 根据索引（即概念id）从concepts列表中获取概念并添加到结果列表中
                if (i < concepts.size() + 1) {
                    combinationReduction.add(concepts.get(i - 1));
                }
            }
        }

        return combinationReduction;
    };

    /**
     * 【新方法】获取所有基数最小的组合约简方案。
     * 首先找到所有约简方案中最小的概念数量，然后返回所有等于该数量的组合约简。
     * @param concepts 完整的概念格
     * @param reductions 多个约简方案的BitSet表示
     * @return 包含所有基数最小的组合约简方案的列表
     */
    public ArrayList<ArrayList<Concept>> getAllCombinationReduction(ArrayList<Concept> concepts, ArrayList<BitSet> reductions) {
        ArrayList<ArrayList<Concept>> allSmallestReductions = new ArrayList<>();
        if (reductions == null || reductions.isEmpty() || concepts == null) {
            return allSmallestReductions;
        }

        // --- 步骤 1: 找到最小基数 ---
        int minCardinality = Integer.MAX_VALUE;
        for (BitSet bs : reductions) {
            int currentCardinality = bs.cardinality();
            if (currentCardinality < minCardinality) {
                minCardinality = currentCardinality;
            }
        }

        // --- 步骤 2: 收集所有基数等于最小基数的约简 ---
        for (BitSet reductionBitSet : reductions) {
            if (reductionBitSet.cardinality() == minCardinality) {
                ArrayList<Concept> currentReduction = new ArrayList<>();
                // 将当前BitSet转换为Concept列表
                for (int i = reductionBitSet.nextSetBit(0); i >= 0; i = reductionBitSet.nextSetBit(i + 1)) {
                    // 假设概念ID从1开始，并且concepts列表是0-indexed且按ID顺序排列
                    if (i > 0 && i <= concepts.size()) {
                        // concepts.get(i-1) 对应 ID 为 i 的概念
                        currentReduction.add(concepts.get(i - 1));
                    }
                }
                allSmallestReductions.add(currentReduction);
            }
        }

        return allSmallestReductions;
    }

    public static void reverseGeneratedContext_temp(Context context, ArrayList<Concept> combinationReduction){
        ArrayList<BitSet> A_g = new ArrayList<>();
        Map<Integer, BitSet> objs = context.getObjs();

        for(int i = 1; i <= context.getObjs_size(); i++){
            A_g.add(objs.get(i));
        }

        for(int i = 0; i < A_g.size(); i++){
            for(int j = 0; j < combinationReduction.size(); j++){
                if(util.is_subset_eq(A_g.get(i), combinationReduction.get(j).getIntent())){
                    int g = i + 1;
                    int B_i = j + 1;
                    System.out.println("(" + g + "," + B_i + ")");
                }
            }
        }
    }

    // Algorithm1
    public static void reverseGeneratedContext(Context context, ArrayList<Concept> combinationReduction, String filename) {
        ArrayList<BitSet> A_g = new ArrayList<>();
        Map<Integer, BitSet> objs = context.getObjs();
        for (int i = 1; i <= context.getObjs_size(); i++) {
            A_g.add(objs.get(i));
        }

        // 使用Map来存储每个对象(g)和其对应的属性集合(B_i)
        Map<Integer, BitSet> objectAttributeMap = new HashMap<>();

        for (int i = 0; i < A_g.size(); i++) {
            for (int j = 0; j < combinationReduction.size(); j++) {
                if (util.is_subset_eq(A_g.get(i), combinationReduction.get(j).getIntent())) {
                    int g = i + 1;
                    int B_i = j + 1;

                    // 如果map中还没有这个对象，就创建一个新的BitSet
                    objectAttributeMap.putIfAbsent(g, new BitSet(combinationReduction.size()));
                    // 将对象g的第B_i个属性设置为1
                    objectAttributeMap.get(g).set(B_i - 1);
                }
            }
        }

        // 构造文件名和路径
        String outputFilename = "src/main/java/data/reverse_context/" + filename + "_reverse.data.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilename))) {
            // 写入第一行：对象总数量,属性总数量
            writer.write(context.getObjs_size() + "," + combinationReduction.size());
            writer.newLine();

            // 写入每个对象的数据
            for (int i = 1; i <= context.getObjs_size(); i++) {
                BitSet attributes = objectAttributeMap.get(i);
                if (attributes == null) {
                    // 如果对象没有任何属性，则输出一行0
                    attributes = new BitSet(combinationReduction.size());
                }

                StringBuilder line = new StringBuilder();
                for (int j = 0; j < combinationReduction.size(); j++) {
                    line.append(attributes.get(j) ? "1" : "0");
                    if (j < combinationReduction.size() - 1) {
                        line.append(",");
                    }
                }
                writer.write(line.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Algorithm2
    public static List<Concept> reverseGeneratedConceptLatticeUsingExtents(ArrayList<Concept> concepts, Context reverseContext, String fileName){
        // 使用 HashSet 替代 ArrayList 来自动去重
        Set<Concept> reverseLatticeSet = new HashSet<>();

        for (Concept concept : concepts) {
            BitSet D = util.get_objs_shared(reverseContext, concept.getExtent());
            BitSet Y = util.get_attrs_shared(reverseContext, D);
            Concept newConcept = new Concept(Y, D);

            // add 方法会自动检查重复。如果 newConcept (基于 Y 和 D) 已存在, 则不会添加
            reverseLatticeSet.add(newConcept);
        }

        // 如果最终需要 ArrayList 结构，可以从 Set 转换
        ArrayList<Concept> reverseLattice = new ArrayList<>(reverseLatticeSet);

        // 为最终的唯一概念重新分配连续的 ID
        for (int i = 0; i < reverseLattice.size(); i++) {
            reverseLattice.get(i).setId(i);
        }

        System.out.println("去重前的概念数量: " + concepts.size());
        System.out.println("去重后的概念数量: " + reverseLattice.size());

        String filePath = "src/main/java/data/reverse_lattice/" + fileName + "_lattice_reverse_extents.data.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Concept concept : reverseLattice) {
                writer.write(concept.toString());
                writer.newLine();
            }
            System.out.println("文件写入成功: " + filePath);

        } catch (IOException e) {
            throw new RuntimeException("文件写入失败: " + e.getMessage(), e);
        }

        return reverseLattice;
    }

    // Algorithm3
    public static List<Concept> reverseGeneratedConceptLatticeUsingIntents(Context context, ArrayList<Concept> concepts, ArrayList<Concept> combinationReduction, String fileName){
        // 使用 HashSet 替代 ArrayList 来自动去重
        Set<Concept> reverseLatticeSet = new HashSet<>();
        for(int i = 0; i < concepts.size(); i++){
            BitSet D = new BitSet();
            BitSet D_temp = new BitSet();
            for (int j = 0; j < combinationReduction.size(); j++) {
                int B_i = j + 1;
                if (util.is_subset_eq(concepts.get(i).getIntent(), combinationReduction.get(j).getIntent())) {
                    D.set(B_i);
                    D_temp = util.union(D_temp, combinationReduction.get(j).getIntent());
                }
            }
            BitSet Y = util.get_attrs_shared(context, D_temp);
            Concept newConcept = new Concept(Y, D);
            reverseLatticeSet.add(newConcept);
        }
        // 如果最终需要 ArrayList 结构，可以从 Set 转换
        ArrayList<Concept> reverseLattice = new ArrayList<>(reverseLatticeSet);

        // 为最终的唯一概念重新分配连续的 ID
        for (int i = 0; i < reverseLattice.size(); i++) {
            reverseLattice.get(i).setId(i);
        }

        System.out.println("去重前的概念数量: " + concepts.size());
        System.out.println("去重后的概念数量: " + reverseLattice.size());

        String filePath = "src/main/java/data/reverse_lattice/" + fileName + "_lattice_reverse_intents.data.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Concept concept : reverseLattice) {
                writer.write(concept.toString());
                writer.newLine();
            }
            System.out.println("文件写入成功: " + filePath);

        } catch (IOException e) {
            throw new RuntimeException("文件写入失败: " + e.getMessage(), e);
        }

        return reverseLattice;
    }
}
