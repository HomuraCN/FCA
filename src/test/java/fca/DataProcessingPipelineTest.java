package fca;

import fca.algorithm.concept.InClose3;
import fca.algorithm.reduction.permutations;
import fca.algorithm.reduction.reduction_qis_Bit;
import fca.utils.Context;
import fca.utils.concept.Concept;
import fca.utils.readFile.BitSetFileHandler;
import fca.utils.readFile.File;
import fca.utils.readFile.PurifyingCSVProcessor;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * 一个集成的测试类，用于按顺序执行完整的数据处理流程：
 * 1. 从CSV文件生成净化的二进制形式背景。
 * 2. 设置阈值
 * 3. 从形式背景生成概念约简。
 */
public class DataProcessingPipelineTest {

    @Test
    public void runFullPipeline() {
        // ======================= 配置区 =======================
        // 在这里修改三个核心参数即可
        String datasetBaseName = "wine_processed"; // 无需.csv后缀
        double percentile = 0.55;
        int numThreads = 16;
        // ===================== 配置区结束 =====================


        // --- 自动生成所有需要的文件名 ---
        // 步骤 1: CSV -> 净化后的形式背景
        String step1_inputCsvFile = datasetBaseName + ".csv";
        String step1_outputContextFile = datasetBaseName + "_deduplication.data.txt";

        // 步骤 2: 形式背景 -> 约简
        String step2_inputContextFile = step1_outputContextFile;
        String step2_outputReductionFile = datasetBaseName + "_deduplication_reduction.data.txt";

        // 步骤 3: 约简 -> 置换后的约简
        String step3_inputReductionFile = step2_outputReductionFile;
        String step3_outputPermutationsFile = datasetBaseName + "_deduplication_reduction_permutations.data.txt";


        try {
            // =================================================================================
            // 步骤 1: 从CSV生成净化的二进制形式背景
            // (源自 NormalizedBinarizerCSVToBinaryContextDeduplicationRowAndCol.java)
            // =================================================================================
            System.out.println("--- [步骤 1/3] 开始从CSV生成净化形式背景 ---");
            String step1_inputFilePath = "src/main/java/data/uci/" + step1_inputCsvFile;
            System.out.println("输入CSV文件: " + step1_inputFilePath);
            System.out.println("使用百分位: " + percentile);

            String step1_outputFilePath = PurifyingCSVProcessor.process(step1_inputFilePath, step1_outputContextFile, percentile);
            
            System.out.println("输出的形式背景文件: " + step1_outputFilePath);
            System.out.println("--- [步骤 1/3] 完成 ---\n");


            // =================================================================================
            // 步骤 2: 从形式背景生成代表概念集 (约简)
            // (源自 BinaryContextToReduction.java)
            // =================================================================================
            System.out.println("--- [步骤 2/3] 开始从形式背景生成约简 ---");
            String step2_inputFilePath = "src/main/java/data/context/" + step2_inputContextFile;
            System.out.println("输入形式背景文件: " + step2_inputFilePath);
            System.out.println("使用线程数: " + numThreads);
            
            // 2.1 加载形式背景
            System.out.println("  > 正在加载形式背景...");
            Context context = File.readFile(step2_inputFilePath);
            System.out.println("  > 加载完成: " + context.getObjs_size() + "个对象, " + context.getAttrs_size() + "个属性。");

            // 2.2 生成所有形式概念
            System.out.println("  > 正在使用 InClose3 生成所有概念...");
            Concept initialConcept = new Concept();
            initialConcept.setExtent(fca.utils.util.makeSet(context.getObjs_size()));
            initialConcept.setIntent(fca.utils.util.get_objs_shared(context, initialConcept.getExtent()));
            Queue<Concept> allConcepts = new LinkedList<>();
            Map<Integer, BitSet> nj = new HashMap<>();
            InClose3.inClose3_exe(context, initialConcept, 1, nj, allConcepts);
            System.out.println("  > 概念生成完成，共 " + allConcepts.size() + " 个概念。");

            // 存储所有形式概念
            String concept_outputFilePath = "src/main/java/data/lattice/" + datasetBaseName + "_deduplication_lattice.data.txt";
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(concept_outputFilePath))) {
                for (Concept concept : allConcepts) {
                    bw.write(concept.toString());
                    bw.newLine();
                }
            }

            // 2.3 运行约简算法
            System.out.println("  > 正在运行 reduction_qis_Bit 约简算法...");
            ArrayList<BitSet> reductionResult = reduction_qis_Bit.reduction_qis_exe_bit(context, allConcepts, numThreads);
            System.out.println("  > 约简完成。");

            // 2.4 存储约简结果
            String step2_outputFilePath = "src/main/java/data/reduction/" + step2_outputReductionFile;
            BitSetFileHandler.writeToFile(reductionResult, step2_outputFilePath);
            System.out.println("输出的约简文件: " + step2_outputFilePath);
            System.out.println("--- [步骤 2/3] 完成 ---\n");


            // =================================================================================
            // 步骤 3: 对约简结果进行置换操作
            // (源自 ReductionToPermutations.java)
            // =================================================================================
            System.out.println("--- [步骤 3/3] 开始对约简进行置换操作 ---");
            String step3_inputFilePath = "src/main/java/data/reduction/" + step3_inputReductionFile;
            System.out.println("输入约简文件: " + step3_inputFilePath);

            // 3.1 加载约简文件
            ArrayList<BitSet> reductionData = BitSetFileHandler.readFromFile(step3_inputFilePath);
            System.out.println("  > 加载了 " + reductionData.size() + " 个BitSet。");

            // 3.2 运行 permutations 算法
            System.out.println("  > 正在运行 permutations 算法...");
            ArrayList<BitSet> permutationsResult = permutations.permutations_exe(reductionData);
            System.out.println("  > 置换处理完成。");

            // 3.3 存储最终结果
            String step3_outputFilePath = "src/main/java/data/reduction_permutations/" + step3_outputPermutationsFile;
            BitSetFileHandler.writeToFile(permutationsResult, step3_outputFilePath);
            System.out.println("输出的置换后约简文件: " + step3_outputFilePath);
            System.out.println("--- [步骤 3/3] 完成 ---\n");

            System.out.println("======== 完整流程执行成功! ========");

        } catch (IOException e) {
            System.err.println("\n在处理过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}