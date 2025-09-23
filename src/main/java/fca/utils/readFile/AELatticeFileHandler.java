package fca.utils.readFile;

import fca.utils.concept.AEConcept_id;

import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 一个用于处理属性导出三支概念格 (AEConcept_id Lattice) 文件存储与读取的工具类。
 * 存储格式基于 AEConcept_id 类的 toString() 方法，读取则使用正则表达式解析。
 */
public class AELatticeFileHandler {

    // 预编译的正则表达式，用于从 AEConcept_id.toString() 的输出中提取信息
    // 捕获组 1: extent 内容
    // 捕获组 2: extent_n 内容
    // 捕获组 3: intent 内容
    // 捕获组 4: id 值
    private static final Pattern AECONCEPT_ID_PATTERN =
            Pattern.compile("AEConcept_R\\{id=(\\d+), extent=\\{(.*?)\\}, extent_n=\\{(.*?)\\}, intent=\\{(.*?)\\}\\}");

    /**
     * 将一个三支概念集合写入到指定的文件。
     * 文件的每一行都是一个 AEConcept_id 对象的 toString() 输出。
     *
     * @param concepts 要写入的三支概念集合
     * @param filePath 输出文件的路径
     * @throws IOException 如果发生文件写入错误
     */
    public static void writeAELatticeToFile(Collection<AEConcept_id> concepts, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (AEConcept_id concept : concepts) {
                writer.write(concept.toString());
                writer.newLine();
            }
        }
    }

    /**
     * 从一个由 AEConcept_id.toString() 格式构成的文件中读取数据，并重建为三支概念列表。
     *
     * @param filePath 输入文件的路径
     * @return 一个包含所有已读取三支概念的 ArrayList
     * @throws IOException 如果发生文件读取错误
     */
    public static ArrayList<AEConcept_id> readAELatticeFromFile(String filePath) throws IOException {
        ArrayList<AEConcept_id> concepts = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = AECONCEPT_ID_PATTERN.matcher(line);

                if (matcher.find()) {
                    // 提取所有捕获组的内容
                    String idString = matcher.group(1);
                    String extentContent = matcher.group(2);
                    String extentNContent = matcher.group(3);
                    String intentContent = matcher.group(4);

                    // 创建并填充新的 AEConcept_id 对象
                    BitSet extent = parseBitSetFromContentString(extentContent);
                    BitSet extent_n = parseBitSetFromContentString(extentNContent);
                    BitSet intent = parseBitSetFromContentString(intentContent);
                    int id = Integer.parseInt(idString);

                    concepts.add(new AEConcept_id(id, extent, extent_n, intent));
                }
            }
        }
        return concepts;
    }

    /**
     * 一个私有的辅助方法，用于从 "1, 2, 3" 这样的字符串内容中解析出 BitSet。
     *
     * @param content BitSet 的内容字符串 (不含花括号)
     * @return 解析后的 BitSet 对象
     */
    private static BitSet parseBitSetFromContentString(String content) {
        BitSet bs = new BitSet();
        if (content != null && !content.isEmpty()) {
            String[] indices = content.split(", ");
            for (String indexStr : indices) {
                if (!indexStr.trim().isEmpty()) {
                    bs.set(Integer.parseInt(indexStr.trim()));
                }
            }
        }
        return bs;
    }
}