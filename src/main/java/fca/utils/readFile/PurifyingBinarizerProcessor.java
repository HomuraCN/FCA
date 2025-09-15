package fca.utils.readFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 该类负责处理混合类型的数据集，并对最终生成的二元背景进行净化（行和列去重）。
 * - 对于数值型列: 归一化到[0,1], 然后根据均值进行二元化。
 * - 对于类别型列: 采用独热编码方式处理。
 * - 最后，移除内容完全相同的行和列。
 */
public class PurifyingBinarizerProcessor {

    public static String process(String inputFilePath, String outputFileName) throws IOException {
        List<String[]> allRows = new ArrayList<>();
        int numAttributes = 0;

        // --- 步骤 1: 读取所有原始数据到内存 ---
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("@")) continue;
                String[] values = line.trim().split(",");
                if (numAttributes == 0) {
                    numAttributes = values.length;
                }
                allRows.add(values);
            }
        }
        if (allRows.isEmpty()) throw new IOException("文件中没有可处理的数据行。");

        // --- 步骤 2: 判断每一列的数据类型 ---
        boolean[] isNumeric = new boolean[numAttributes];
        Arrays.fill(isNumeric, true);
        String[] col = allRows.getFirst();
        for (int i = 0; i < numAttributes; i++) {
            try {
                Double.parseDouble(col[i]);
            } catch (NumberFormatException e) {
                isNumeric[i] = false;
                break;
            }
        }

        // --- 步骤 3: 预处理 - 计算均值 & 发现唯一的类别值 ---
        double[] means = new double[numAttributes];
        Map<Integer, Set<String>> categoricalUniqueValues = new HashMap<>();
        for (int j = 0; j < numAttributes; j++) {
            if (isNumeric[j]) {
                double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
                for (String[] row : allRows) {
                    double val = Double.parseDouble(row[j]);
                    if (val < min) min = val;
                    if (val > max) max = val;
                }
                double range = max - min;
                double sumOfNormalized = 0.0;
                for (String[] row : allRows) {
                    double val = Double.parseDouble(row[j]);
                    sumOfNormalized += (range == 0) ? 0 : (val - min) / range;
                }
                means[j] = sumOfNormalized / allRows.size();
            } else {
                Set<String> uniqueVals = new HashSet<>();
                for (String[] row : allRows) {
                    uniqueVals.add(row[j]);
                }
                categoricalUniqueValues.put(j, uniqueVals);
            }
        }

        // --- 步骤 4: 构建新属性的映射表 ---
        LinkedHashMap<String, Integer> finalAttributeMap = new LinkedHashMap<>();
        for (int j = 0; j < numAttributes; j++) {
            if (isNumeric[j]) {
                String key = "attr" + j;
                finalAttributeMap.put(key, finalAttributeMap.size());
            } else {
                List<String> sortedValues = new ArrayList<>(categoricalUniqueValues.get(j));
                Collections.sort(sortedValues);
                for (String value : sortedValues) {
                    String key = "attr" + j + ":" + value;
                    finalAttributeMap.put(key, finalAttributeMap.size());
                }
            }
        }

        // --- 步骤 5: 创建并填充临时的二元矩阵 ---
        int numObjects = allRows.size();
        int generatedNumAttributes = finalAttributeMap.size();
        int[][] binaryData = new int[numObjects][generatedNumAttributes];
        for (int i = 0; i < numObjects; i++) {
            String[] row = allRows.get(i);
            for (int j = 0; j < numAttributes; j++) {
                if (isNumeric[j]) {
                    double val = Double.parseDouble(row[j]);
                    double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
                    for (String[] r : allRows) {
                        double v = Double.parseDouble(r[j]);
                        if (v < min) min = v;
                        if (v > max) max = v;
                    }
                    double range = max - min;
                    double normalizedVal = (range == 0) ? 0 : (val - min) / range;
                    int value = (normalizedVal < means[j]) ? 0 : 1;
                    String key = "attr" + j;
                    int colIdx = finalAttributeMap.get(key);
                    binaryData[i][colIdx] = value;
                } else {
                    String value = row[j];
                    String key = "attr" + j + ":" + value;
                    int colIdx = finalAttributeMap.get(key);
                    binaryData[i][colIdx] = 1;
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
            for (int j = 0; j < generatedNumAttributes; j++) {
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
