package fca.utils.readFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * 该类负责按照指定规则处理数值型数据集：
 * 1. 归一化到[0, 1]区间
 * 2. 计算每列的均值
 * 3. 根据与均值的比较进行二元化 (0/1)
 */
public class BinarizeProcessor {

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

        // --- 步骤 2: 判断每一列的数据类型 (数值型 vs 类别型) ---
        boolean[] isNumeric = new boolean[numAttributes];
        Arrays.fill(isNumeric, true); // 先假设所有列都是数值型
        for (int j = 0; j < numAttributes; j++) {
            for (String[] row : allRows) {
                try {
                    Double.parseDouble(row[j]);
                } catch (NumberFormatException e) {
                    isNumeric[j] = false; // 只要有一次解析失败，就标记为类别型
                    break;
                }
            }
        }

        // --- 步骤 3: 预处理 - 计算均值 & 发现唯一的类别值 ---
        double[] means = new double[numAttributes];
        // 存储每个类别列的唯一值
        Map<Integer, Set<String>> categoricalUniqueValues = new HashMap<>();

        for (int j = 0; j < numAttributes; j++) {
            if (isNumeric[j]) {
                // 处理数值列：归一化并计算均值
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
                // 处理类别列：找到所有唯一值
                Set<String> uniqueVals = new HashSet<>();
                for (String[] row : allRows) {
                    uniqueVals.add(row[j]);
                }
                categoricalUniqueValues.put(j, uniqueVals);
            }
        }

        // --- 步骤 4: 构建新属性的映射表 ---
        LinkedHashMap<String, Integer> finalAttributeMap = new LinkedHashMap<>(); // 使用LinkedHashMap保持顺序
        for (int j = 0; j < numAttributes; j++) {
            if (isNumeric[j]) {
                // 数值列只产生一个新属性
                String key = "attr" + j; // 可以根据需要命名得更详细
                finalAttributeMap.put(key, finalAttributeMap.size());
            } else {
                // 类别列为每个唯一值产生一个新属性
                for (String value : categoricalUniqueValues.get(j)) {
                    String key = "attr" + j + ":" + value; // 仿照UCIParser的命名
                    finalAttributeMap.put(key, finalAttributeMap.size());
                }
            }
        }

        // --- 步骤 5: 创建并填充最终的二元矩阵 ---
        int numObjects = allRows.size();
        int finalNumAttributes = finalAttributeMap.size();
        int[][] binaryData = new int[numObjects][finalNumAttributes];

        for (int i = 0; i < numObjects; i++) {
            String[] row = allRows.get(i);
            for (int j = 0; j < numAttributes; j++) {
                if (isNumeric[j]) {
                    // 数值列的处理
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
                    // 类别列的处理 (独热编码)
                    String value = row[j];
                    String key = "attr" + j + ":" + value;
                    int colIdx = finalAttributeMap.get(key);
                    binaryData[i][colIdx] = 1; // 对应的值设为1，其他自动为0
                }
            }
        }

        // --- 步骤 6: 将结果写入文件 ---
        String outputDir = "src/main/java/data/context/";
        Files.createDirectories(Paths.get(outputDir));
        String finalOutputPath = outputDir + outputFileName;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(finalOutputPath))) {
            writer.write(numObjects + "," + finalNumAttributes);
            writer.newLine();
            for (int i = 0; i < numObjects; i++) {
                StringBuilder lineBuilder = new StringBuilder();
                for (int j = 0; j < finalNumAttributes; j++) {
                    lineBuilder.append(binaryData[i][j]);
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