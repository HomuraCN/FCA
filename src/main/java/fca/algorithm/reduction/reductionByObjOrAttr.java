package fca.algorithm.reduction;

import fca.utils.Context;
import fca.utils.concept.Concept;
import fca.utils.readFile.File;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static fca.algorithm.reduction.permutations.permutations_exe;
import static fca.utils.util.*;

public class reductionByObjOrAttr {
    public static void main(String[] args) {
        // --- 1. 配置输入和输出文件 ---
        String inputFileName = "iris.data.txt";
        String inputFilePath = "src/main/java/data/context/" + inputFileName;

        String outputDir = "src/main/java/data/lattice/";
        String baseName = inputFileName;
        // 检查并移除后缀
        if (baseName.endsWith(".data.txt")) {
            baseName = baseName.substring(0, baseName.length() - ".data.txt".length());
        } else if (baseName.endsWith(".txt")) {
            baseName = baseName.substring(0, baseName.length() - ".txt".length());
        }

        // 输出文件名，例如 iris_concepts.txt
        String outputFileName = baseName + "_reduction_lattice.data.txt";
        String outputFilePath = outputDir + outputFileName;

        System.out.println("--- 开始执行 Reduction 算法 ---");
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

            // --- 4. 生成概念 (Queue<Concept>) ---
            System.out.println("\n正在生成概念...");
            startTime = System.currentTimeMillis();

            Queue<Concept> concepts = new LinkedList<>();
            int objs_size = context.getObjs_size();
            int attr_size = context.getAttrs_size();
            String conceptType;

            if (objs_size >= attr_size) {
                conceptType = "属性概念 (Attribute Concepts)";
                System.out.println("正在生成: " + conceptType);
                for (int i = 1; i <= attr_size; i++) {
                    Concept concept = new Concept();
                    concept.setId(i);
                    concept.setExtent(context.getAttrs().get(i));
                    concept.setIntent(get_objs_shared(context, concept.getExtent()));
                    concepts.add(concept);
                }
            } else {
                conceptType = "对象概念 (Object Concepts)";
                System.out.println("正在生成: " + conceptType);
                for (int i = 1; i <= objs_size; i++) {
                    Concept concept = new Concept();
                    concept.setId(i);
                    concept.setIntent(context.getObjs().get(i));
                    concept.setExtent(get_attrs_shared(context, concept.getIntent()));
                    concepts.add(concept);
                }
            }
            endTime = System.currentTimeMillis();
            System.out.println("概念生成完成，共 " + concepts.size() + " 个概念。耗时: " + (endTime - startTime) + "ms.");

            // --- 5. 将结果写入文件 (Queue<Concept>) ---
            // 按照您的要求，这里遍历 Queue<Concept> 并写入文件
            System.out.println("\n正在将概念写入到输出文件...");
            startTime = System.currentTimeMillis();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
                for (Concept concept : concepts) {
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