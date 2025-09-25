package fca.utils.readFile;

import fca.utils.Context;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class PurifyingBinaryContextProcessor {
    public static String process(String inputFilePath, String outputFileName) throws IOException {
        Context context = File.readFile(inputFilePath);

        // --- 步骤 5: 创建并填充临时的二元矩阵 ---
        // 1. 根据 context 的尺寸，初始化一个全为 0 的二维数组
        int numObjs = context.getObjs_size();
        int numAttrs = context.getAttrs_size();
        int[][] binaryData = new int[numObjs][numAttrs];

        // 2. 获取核心数据：从对象索引到其属性 BitSet 的映射
        Map<Integer, BitSet> objectMap = context.getObjs();

        // 3. 遍历 Map 中的每一个对象（每一行）
        // entry.getKey() 是对象索引 (行号 i)
        // entry.getValue() 是该对象拥有的属性集合 (BitSet)
        for (Map.Entry<Integer, BitSet> entry : objectMap.entrySet()) {
            int rowIndex = entry.getKey();
            BitSet attributes = entry.getValue();

            // 确保行号在数组范围内
            if (rowIndex < numObjs) {
                // 4. 高效遍历 BitSet 中所有被设置为 true 的位（即值为 1 的位置）
                // colIndex 就是该对象拥有的属性索引 (列号 j)
                // nextSetBit(i) 方法会从索引 i 开始查找下一个为 1 的位的索引
                for (int colIndex = attributes.nextSetBit(0); colIndex >= 0; colIndex = attributes.nextSetBit(colIndex + 1)) {

                    // 确保列号也在数组范围内
                    if (colIndex < numAttrs) {
                        // 5. 在二维数组的对应位置 [i][j] 标记为 1
                        binaryData[rowIndex][colIndex] = 1;
                    }
                }
            }
        }

        // --- 步骤 6: 移除重复的行 ---
        Set<List<Integer>> uniqueRowsSet = new HashSet<>();
        List<int[]> uniqueRowsList = new ArrayList<>();
        for (int[] row : binaryData) {
            List<Integer> rowAsList = Arrays.stream(row).boxed().collect(Collectors.toList());
            if (uniqueRowsSet.add(rowAsList)) {
                uniqueRowsList.add(row);
            }
        }
        int[][] rowDeduplicatedData = uniqueRowsList.toArray(new int[0][]);

        // --- 步骤 7: 移除重复的列 ---
        Set<List<Integer>> uniqueColumnsSet = new HashSet<>();
        List<Integer> columnsToKeepIndices = new ArrayList<>();
        int rowCountAfterDeduplication = rowDeduplicatedData.length;
        if (rowCountAfterDeduplication > 0) {
            for (int j = 0; j < context.getAttrs_size(); j++) {
                List<Integer> columnAsList = new ArrayList<>();
                for (int i = 0; i < rowCountAfterDeduplication; i++) {
                    columnAsList.add(rowDeduplicatedData[i][j]);
                }
                if (uniqueColumnsSet.add(columnAsList)) {
                    columnsToKeepIndices.add(j);
                }
            }
        }

        // --- 步骤 8: 构建最终的净化矩阵 ---
        int finalNumObjects = rowCountAfterDeduplication;
        int finalNumAttributes = columnsToKeepIndices.size();
        int[][] purifiedData = new int[finalNumObjects][finalNumAttributes];
        for (int i = 0; i < finalNumObjects; i++) {
            for (int j = 0; j < finalNumAttributes; j++) {
                int originalColumnIndex = columnsToKeepIndices.get(j);
                purifiedData[i][j] = rowDeduplicatedData[i][originalColumnIndex];
            }
        }

        // --- 步骤 9: 将最终的净化结果写入文件 ---
        String outputDir = "src/main/java/data/context/";
        Files.createDirectories(Paths.get(outputDir));
        String finalOutputPath = outputDir + outputFileName;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(finalOutputPath))) {
            writer.write(finalNumObjects + "," + finalNumAttributes);
            writer.newLine();
            for (int[] row : purifiedData) {
                StringBuilder lineBuilder = new StringBuilder();
                for (int j = 0; j < finalNumAttributes; j++) {
                    lineBuilder.append(row[j]);
                    if (j < finalNumAttributes - 1) {
                        lineBuilder.append(",");
                    }
                }
                writer.write(lineBuilder.toString());
                writer.newLine();
            }
        }
        return finalOutputPath;
    }
}
