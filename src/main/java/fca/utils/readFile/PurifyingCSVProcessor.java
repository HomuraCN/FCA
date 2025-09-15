package fca.utils.readFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 该类负责处理带标题行的CSV文件，并对数据进行净化（行和列去重）。
 * - 自动忽略CSV文件的第一行（标题行）。
 * - 对于数值型列: 归一化到[0,1], 然后根据均值进行二元化。
 * - 对于类别型列: 采用独热编码方式处理。
 * - 最后，移除内容完全相同的行和列。
 */
public class PurifyingCSVProcessor {

    public static String process(String inputFilePath, String outputFileName) throws IOException {
        List<String[]> allDataRows = new ArrayList<>();
        String[] headers = null;
        int numAttributes = 0;

        // --- 步骤 1: 读取CSV文件，自动跳过第一行标题 ---
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            // 读取并存储标题行
            headers = reader.readLine().split(",");
            numAttributes = headers.length;

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                allDataRows.add(line.trim().split(","));
            }
        }
        if (allDataRows.isEmpty()) throw new IOException("文件中没有可处理的数据行。");

        // --- 步骤 2: 判断每一列的数据类型 ---
        boolean[] isNumeric = new boolean[numAttributes];
        Arrays.fill(isNumeric, true);
        String[] col = allDataRows.getFirst();
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
                for (String[] row : allDataRows) {
                    double val = Double.parseDouble(row[j]);
                    if (val < min) min = val;
                    if (val > max) max = val;
                }
                double range = max - min;
                double sumOfNormalized = 0.0;
                for (String[] row : allDataRows) {
                    double val = Double.parseDouble(row[j]);
                    sumOfNormalized += (range == 0) ? 0 : (val - min) / range;
                }
                means[j] = sumOfNormalized / allDataRows.size();
            } else {
                Set<String> uniqueVals = new HashSet<>();
                for (String[] row : allDataRows) {
                    uniqueVals.add(row[j]);
                }
                categoricalUniqueValues.put(j, uniqueVals);
            }
        }

        // --- 步骤 4: 构建新属性的映射表 (使用CSV的标题) ---
        LinkedHashMap<String, Integer> finalAttributeMap = new LinkedHashMap<>();
        for (int j = 0; j < numAttributes; j++) {
            String headerName = headers[j].trim();
            if (isNumeric[j]) {
                String key = headerName;
                finalAttributeMap.put(key, finalAttributeMap.size());
            } else {
                List<String> sortedValues = new ArrayList<>(categoricalUniqueValues.get(j));
                Collections.sort(sortedValues);
                for (String value : sortedValues) {
                    String key = headerName + ":" + value;
                    finalAttributeMap.put(key, finalAttributeMap.size());
                }
            }
        }

        // --- 步骤 5: 创建并填充临时的二元矩阵 ---
        int numObjects = allDataRows.size();
        int generatedNumAttributes = finalAttributeMap.size();
        int[][] binaryData = new int[numObjects][generatedNumAttributes];
        for (int i = 0; i < numObjects; i++) {
            String[] row = allDataRows.get(i);
            for (int j = 0; j < numAttributes; j++) {
                String headerName = headers[j].trim();
                if (isNumeric[j]) {
                    double val = Double.parseDouble(row[j]);
                    double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
                    for (String[] r : allDataRows) {
                        double v = Double.parseDouble(r[j]);
                        if (v < min) min = v;
                        if (v > max) max = v;
                    }
                    double range = max - min;
                    double normalizedVal = (range == 0) ? 0 : (val - min) / range;
                    int value = (normalizedVal < means[j]) ? 0 : 1;
                    String key = headerName;
                    int colIdx = finalAttributeMap.get(key);
                    binaryData[i][colIdx] = value;
                } else {
                    String value = row[j];
                    String key = headerName + ":" + value;
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
