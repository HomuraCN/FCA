package fca.utils.dataProcess;

import fca.algorithm.concept.InClose3;
import fca.algorithm.reduction.reduction_qis_Bit;
import fca.utils.Context;
import fca.utils.concept.Concept;
import fca.utils.readFile.BitSetFileHandler;
import fca.utils.readFile.File;

import java.io.IOException;
import java.util.*;

// 形式背景 -> 代表概念集
public class BinaryContextToReduction {
    public static void main(String[] args) {
        String fileName = "Maternal Health Risk Data Set_deduplication.data.txt";
        String filePath = "src/main/java/data/context/" + fileName;
        int numThreads = 16; // 您可以根据您的CPU核心数调整此值

        System.out.println("reduction 算法...");
        System.out.println("使用数据集: " + filePath);
        System.out.println("使用线程数: " + numThreads);

        try {
            // ==================== 步骤 1: 加载形式背景 ====================
            long startTime = System.currentTimeMillis();
            Context context = File.readFile(filePath);
            long endTime = System.currentTimeMillis();
            System.out.println("\n[步骤 1] 形式背景加载完成。");
            System.out.println("耗时: " + (endTime - startTime) + "ms");
            System.out.println("对象数: " + context.getObjs_size() + ", 属性数: " + context.getAttrs_size());

            // ==================== 步骤 2: 生成所有形式概念 ====================
            // 这是为约简算法准备输入数据的关键一步
            System.out.println("\n[步骤 2] 开始生成所有形式概念 (使用 InClose3 算法)...");
            startTime = System.currentTimeMillis();

            Concept initialConcept = new Concept();
            initialConcept.setExtent(fca.utils.util.makeSet(context.getObjs_size()));
            initialConcept.setIntent(fca.utils.util.get_objs_shared(context, initialConcept.getExtent()));

            Queue<Concept> allConcepts = new LinkedList<>();
            Map<Integer, BitSet> nj = new HashMap<>(); // InClose3 需要的参数

            InClose3.inClose3_exe(context, initialConcept, 1, nj, allConcepts);

            endTime = System.currentTimeMillis();
            System.out.println("概念生成完成。");
            System.out.println("共生成 " + allConcepts.size() + " 个概念。");
            System.out.println("耗时: " + (endTime - startTime) + "ms");


            // ==================== 步骤 3: 运行 reduction_qis_Bit 约简算法 ====================
            System.out.println("\n[步骤 3] 开始运行 reduction_qis_Bit 约简算法...");
            startTime = System.currentTimeMillis();

            ArrayList<BitSet> reductionResult = reduction_qis_Bit.reduction_qis_exe_bit(context, allConcepts, numThreads);

            endTime = System.currentTimeMillis();
            System.out.println("约简完成。");
            System.out.println("耗时: " + (endTime - startTime) + "ms");

            System.out.println("\n[步骤 3.5] 存储:");
            startTime = System.currentTimeMillis();
            // 检查并移除已知的后缀，如 .data.txt 或 .txt
            fileName = fileName.substring(0, fileName.length() - ".data.txt".length());
            fileName = fileName + "_reduction.data.txt";
            filePath = "src/main/java/data/reduction/" + fileName;
            BitSetFileHandler.writeToFile(reductionResult, filePath);
            endTime = System.currentTimeMillis();
            System.out.println("存储耗时: " + (endTime - startTime) + "ms");

            // ==================== 步骤 4: 显示输出结果 ====================
            System.out.println("\n[步骤 4] 算法输出结果:");
            if (reductionResult != null) {
                System.out.println("约简后得到 " + reductionResult.size() + " 个最小代表概念集。");
                System.out.println("--- 显示前10个代表集 (BitSet中的数字代表概念ID) ---");
                for (int i = 0; i < Math.min(10, reductionResult.size()); i++) {
                    System.out.println("代表集 " + (i + 1) + ": " + reductionResult.get(i));
                }
            } else {
                System.out.println("算法未返回结果。");
            }

            System.out.println("\n[步骤 4.5] 读取并输出结果:");
            startTime = System.currentTimeMillis();
            ArrayList<BitSet> bitSetArrayList = BitSetFileHandler.readFromFile(filePath);
            System.out.println("约简后得到 " + bitSetArrayList.size() + " 个最小代表概念集。");
            System.out.println("--- 显示前10个代表集 (BitSet中的数字代表概念ID) ---");
            for (int i = 0; i < Math.min(10, bitSetArrayList.size()); i++) {
                System.out.println("代表集 " + (i + 1) + ": " + bitSetArrayList.get(i));
            }
            endTime = System.currentTimeMillis();
            System.out.println("读取耗时: " + (endTime - startTime) + "ms");

        } catch (IOException e) {
            System.err.println("\n处理文件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
