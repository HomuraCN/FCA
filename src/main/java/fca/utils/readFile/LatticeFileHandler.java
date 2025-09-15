package fca.utils.readFile;

import fca.utils.concept.Concept;

import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 一个用于处理概念格（Lattice）文件存储与读取的工具类。
 * 存储格式基于 Concept 类的 toString() 方法，读取则使用正则表达式解析。
 */
public class LatticeFileHandler {

    // 预编译的正则表达式，用于从 Concept.toString() 的输出中提取信息
    // 捕获组 1: extent 内容
    // 捕获组 2: intent 内容
    // 捕获组 3: id 值
    private static final Pattern CONCEPT_PATTERN =
            Pattern.compile("Concept\\{extent=\\{(.*?)\\}, intent=\\{(.*?)\\}, id=(\\d+)\\}");

    /**
     * 将一个概念集合（如队列、列表等）写入到指定的文件。
     * 文件的每一行都是一个 Concept 对象的 toString() 输出。
     *
     * @param concepts 要写入的概念集合
     * @param filePath 输出文件的路径
     * @throws IOException 如果发生文件写入错误
     */
    public static void writeLatticeToFile(Collection<Concept> concepts, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // 可以选择性地写入文件头信息
            writer.write("--- Total Concepts: " + concepts.size() + " ---\n");
            for (Concept concept : concepts) {
                writer.write(concept.toString());
                writer.newLine();
            }
        }
    }

//    /**
//     * 从一个由 Concept.toString() 格式构成的文件中读取数据，并重建为概念队列。
//     *
//     * @param filePath 输入文件的路径
//     * @return 一个包含所有已读取概念的队列 (Queue)
//     * @throws IOException 如果发生文件读取错误
//     */
//    public static Queue<Concept> readLatticeFromFile(String filePath) throws IOException {
//        Queue<Concept> concepts = new LinkedList<>();
//        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                // 使用正则表达式匹配器来处理每一行
//                Matcher matcher = CONCEPT_PATTERN.matcher(line);
//
//                // 如果当前行成功匹配了我们定义的格式
//                if (matcher.find()) {
//                    // 提取捕获组的内容
//                    String extentContent = matcher.group(1);
//                    String intentContent = matcher.group(2);
//                    String idString = matcher.group(3);
//
//                    // 创建并填充新的 Concept 对象
//                    Concept concept = new Concept();
//                    concept.setId(Integer.parseInt(idString));
//                    concept.setExtent(parseBitSetFromContentString(extentContent));
//                    concept.setIntent(parseBitSetFromContentString(intentContent));
//
//                    concepts.add(concept);
//                }
//                // 非匹配行（如文件头或其他注释）将被自动忽略
//            }
//        }
//        return concepts;
//    }
    /**
     * [已修改] 从文件中读取数据，并重建为概念 ArrayList。
     *
     * @param filePath 输入文件的路径
     * @return 一个包含所有已读取概念的 ArrayList
     * @throws IOException 如果发生文件读取错误
     */
    public static ArrayList<Concept> readLatticeFromFile(String filePath) throws IOException {
        // 1. 将集合类型从 LinkedList 更改为 ArrayList
        ArrayList<Concept> concepts = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = CONCEPT_PATTERN.matcher(line);
                if (matcher.find()) {
                    String extentContent = matcher.group(1);
                    String intentContent = matcher.group(2);
                    String idString = matcher.group(3);

                    Concept concept = new Concept();
                    concept.setId(Integer.parseInt(idString));
                    concept.setExtent(parseBitSetFromContentString(extentContent));
                    concept.setIntent(parseBitSetFromContentString(intentContent));

                    // concepts.add() 方法对于 ArrayList 和 LinkedList 通用
                    concepts.add(concept);
                }
            }
        }
        // 2. 返回类型现在是 ArrayList<Concept>
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
            // 按 ", " 分割字符串
            String[] indices = content.split(", ");
            for (String indexStr : indices) {
                // 确保字符串不为空，以防万一
                if (!indexStr.trim().isEmpty()) {
                    bs.set(Integer.parseInt(indexStr.trim()));
                }
            }
        }
        return bs;
    }
}
