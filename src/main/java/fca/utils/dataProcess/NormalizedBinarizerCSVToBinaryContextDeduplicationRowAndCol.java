package fca.utils.dataProcess;

import fca.utils.readFile.PurifyingCSVProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// 原始.csv后缀数据 -> 形式背景(净化背景)
public class NormalizedBinarizerCSVToBinaryContextDeduplicationRowAndCol {

    public static void main(String[] args) {
        // --- 配置区 ---
        // 1. 设置要处理的原始CSV文件名 (假设在 data/uci/ 目录下)
        String uciDatasetName = "heart_disease_processed.csv";
        double percentile = 0.6;

        // 2. 自动生成输出文件名
        String baseName = uciDatasetName.endsWith(".csv") ? uciDatasetName.substring(0, uciDatasetName.length() - 4) : uciDatasetName;
        String outputContextName = baseName + "_deduplication.data.txt";
        // --- 配置结束 ---


        String inputFilePath = "src/main/java/data/uci/" + uciDatasetName;
        System.out.println(uciDatasetName + " CSV数据集转换 (混合处理 + 行列去重)...");
        System.out.println("原始文件路径: " + inputFilePath);

        try {
            // 调用新的、专门处理CSV的净化方法
            String outputFilePath = PurifyingCSVProcessor.process(inputFilePath, outputContextName, percentile);
            System.out.println("输出的形式背景文件路径: " + outputFilePath);

            Path path = Paths.get(outputFilePath);
            if (Files.exists(path)) {
                System.out.println("\n文件内容预览 (前5行):");
                Files.lines(path).limit(5).forEach(System.out::println);
            } else {
                System.out.println("错误: 输出文件未成功创建。");
            }
            System.out.println("\n转换成功!");

        } catch (IOException e) {
            System.err.println("\n在处理文件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
