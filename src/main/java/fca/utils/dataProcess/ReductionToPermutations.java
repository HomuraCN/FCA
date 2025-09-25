package fca.utils.dataProcess;

import fca.algorithm.reduction.permutations;
import fca.utils.readFile.BitSetFileHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.BitSet;

/**
 * 该类作为一个可执行的入口点，用于将 'permutations' 算法应用于给定的约简文件。
 * 它读取一个约简（一个 BitSet 列表），处理它以找到排列组合，
 * 并将结果写入一个新文件。
 */
// 代表概念集 -> 概念约简
public class ReductionToPermutations {
    public static void main(String[] args) {
        // --- 1. 配置 ---
        // 您可以在此处更改为您想要处理的约简文件。
        String reductionFileName = "200_8_50_deduplication_reduction.data.txt";
        String inputFilePath = "src/main/java/data/reduction/" + reductionFileName;

        System.out.println("Permutations 算法...");
        System.out.println("使用约简文件: " + inputFilePath);

        try {
            // --- 2. 加载约简文件 ---
            long startTime = System.currentTimeMillis();
            ArrayList<BitSet> reductionData = BitSetFileHandler.readFromFile(inputFilePath);
            long endTime = System.currentTimeMillis();
            System.out.println("\n[步骤 1] 约简文件加载完成。");
            System.out.println("耗时: " + (endTime - startTime) + "ms");
            System.out.println("共加载 " + reductionData.size() + " 个BitSet。");

            // --- 3. 运行 Permutations 算法 ---
            System.out.println("\n[步骤 2] 开始运行 permutations 算法...");
            startTime = System.currentTimeMillis();

            ArrayList<BitSet> permutationsResult = permutations.permutations_exe(reductionData);

            endTime = System.currentTimeMillis();
            System.out.println("Permutations 处理完成。");
            System.out.println("耗时: " + (endTime - startTime) + "ms");

            // --- 4. 存储结果 ---
            System.out.println("\n[步骤 3] 存储结果...");
            startTime = System.currentTimeMillis();

            // 创建输出文件名
            String baseName = reductionFileName.replace("_reduction.data.txt", "");
            String outputFileName = baseName + "_reduction_permutations.data.txt";
            String outputDir = "src/main/java/data/reduction_permutations/";

            // 确保输出目录存在
            Files.createDirectories(Paths.get(outputDir));
            String finalOutputPath = outputDir + outputFileName;

            BitSetFileHandler.writeToFile(permutationsResult, finalOutputPath);

            endTime = System.currentTimeMillis();
            System.out.println("存储完成。输出路径: " + finalOutputPath);
            System.out.println("存储耗时: " + (endTime - startTime) + "ms");

            // --- 5. 显示输出 ---
            System.out.println("\n[步骤 4] 算法输出结果:");
            if (permutationsResult != null) {
                System.out.println("处理后得到 " + permutationsResult.size() + " 个BitSet。");
                System.out.println("--- 显示前10个结果 ---");
                for (int i = 0; i < Math.min(10, permutationsResult.size()); i++) {
                    System.out.println("结果 " + (i + 1) + ": " + permutationsResult.get(i));
                }
            } else {
                System.out.println("算法未返回结果。");
            }

        } catch (IOException e) {
            System.err.println("\n处理文件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}