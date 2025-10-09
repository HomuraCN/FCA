package fca.utils.dataProcess;

import fca.utils.readFile.UCIParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// 原始.data后缀数据 -> 形式背景(非净化背景)
public class DataToBinaryContext {
    public static void main(String[] args) {
        // 转换的数据集文件名
        String datasetName = "zoo.data";

        System.out.println(datasetName + " 数据集转换...");
        System.out.println("原始文件路径: src/main/java/data/uci/" + datasetName);

        try {
            // 调用UciParser方法
            String outputFilePath = UCIParser.UciParser(datasetName);
            System.out.println("输出的形式背景文件路径: " + outputFilePath);

            // 检查文件是否存在并读取前几行内容进行验证
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
