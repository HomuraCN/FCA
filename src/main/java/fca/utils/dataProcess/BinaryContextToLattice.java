package fca.utils.dataProcess;

import fca.algorithm.concept.InClose3;
import fca.utils.Context;
import fca.utils.concept.Concept;
import fca.utils.readFile.File;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

// 形式背景 -> 建格
public class BinaryContextToLattice {
    public static void main(String[] args) {
        // --- 1. 配置输入和输出文件 ---
        // 您可以在这里更改要处理的文件名
        String inputFileName = "heart_disease_processed_deduplication.data.txt";
        String inputFilePath = "src/main/java/data/context/" + inputFileName;

        String outputDir = "src/main/java/data/lattice/";
        String baseName = inputFileName;
        // 检查并移除已知的后缀，如 .data.txt 或 .txt
        baseName = baseName.substring(0, baseName.length() - ".data.txt".length());

        // 拼接成新的文件名
        String outputFileName = baseName + "_lattice.data.txt";
        String outputFilePath = outputDir + outputFileName;

        System.out.println("--- 开始从形式背景生成概念格 ---");
        System.out.println("输入文件: " + inputFilePath);
        System.out.println("输出文件: " + outputFilePath);

        try {
            // --- 2. 确保输出目录存在 ---
            Files.createDirectories(Paths.get(outputDir));

            // --- 3. 加载形式背景 ---
            System.out.println("\n正在加载形式背景...");
            long startTime = System.currentTimeMillis();
            Context context = File.readFile(inputFilePath);
            long endTime = System.currentTimeMillis();
            System.out.println("加载完成，耗时 " + (endTime - startTime) + "ms.");
            System.out.println("形式背景统计: " + context.getObjs_size() + " 个对象, " + context.getAttrs_size() + " 个属性.");

            // --- 4. 准备并调用 inClose3_exe 算法 ---
            System.out.println("\n正在使用 InClose3 算法生成概念...");
            startTime = System.currentTimeMillis();

            // 初始化第一个概念
            Concept initialConcept = new Concept();
            initialConcept.setExtent(fca.utils.util.makeSet(context.getObjs_size()));
            initialConcept.setIntent(fca.utils.util.get_objs_shared(context, initialConcept.getExtent()));

            // 准备参数
            Queue<Concept> allConcepts = new LinkedList<>();
            Map<Integer, BitSet> nj = new HashMap<>();

            // 调用算法，结果将填充到 allConcepts 队列中
            InClose3.inClose3_exe(context, initialConcept, 1, nj, allConcepts);

            endTime = System.currentTimeMillis();
            System.out.println("概念生成完成！");
            System.out.println("共生成 " + allConcepts.size() + " 个概念。");
            System.out.println("耗时: " + (endTime - startTime) + "ms.");

            // --- 5. 将结果写入文件 ---
            System.out.println("\n正在将所有概念写入到输出文件...");
            startTime = System.currentTimeMillis();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
                for (Concept concept : allConcepts) {
                    writer.write(concept.toString());
                    writer.newLine();
                }
            }

            endTime = System.currentTimeMillis();
            System.out.println("文件写入成功！耗时: " + (endTime - startTime) + "ms.");

        } catch (IOException e) {
            System.err.println("\n处理过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
