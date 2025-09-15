package fca.utils.readFile;

import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;

public class BitSetFileHandler {

    /**
     * 将 ArrayList<BitSet> 写入到文件中，每行一个BitSet。
     * @param data 要写入的数据
     * @param filePath 文件路径
     * @throws IOException 写入异常
     */
    public static void writeToFile(ArrayList<BitSet> data, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (BitSet bs : data) {
                writer.write(bs.toString());
                writer.newLine();
            }
        }
    }

    /**
     * 从文件中读取数据，重建 ArrayList<BitSet>。
     * @param filePath 文件路径
     * @return 读取到的数据
     * @throws IOException 读取异常
     */
    public static ArrayList<BitSet> readFromFile(String filePath) throws IOException {
        ArrayList<BitSet> data = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                BitSet bs = new BitSet();
                // 从 "{0, 5, 10}" 这样的字符串中解析出数字
                if (line.length() > 2) { // 忽略空的 "{}"
                    String content = line.substring(1, line.length() - 1); // 移除花括号
                    String[] indices = content.split(", "); // 按逗号和空格分割
                    for (String indexStr : indices) {
                        if (!indexStr.isEmpty()) {
                            int index = Integer.parseInt(indexStr.trim());
                            bs.set(index);
                        }
                    }
                }
                data.add(bs);
            }
        }
        return data;
    }
}