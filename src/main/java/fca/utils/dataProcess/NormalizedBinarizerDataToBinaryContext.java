package fca.utils.dataProcess;

import fca.utils.readFile.BinarizeProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NormalizedBinarizerDataToBinaryContext {

    public static void main(String[] args) {
        // --- 配置区 ---
        String uciDatasetName = "iris.data";
        String outputContextName = "iris_mixed_processed.data.txt";
        // --- 配置结束 ---

        String inputFilePath = "src/main/java/data/uci/" + uciDatasetName;
        System.out.println(uciDatasetName + " 数据集转换 (混合类型处理)...");
        System.out.println("原始文件路径: " + inputFilePath);

        try {
            // 调用新的混合类型处理方法
            String outputFilePath = BinarizeProcessor.process(inputFilePath, outputContextName);
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