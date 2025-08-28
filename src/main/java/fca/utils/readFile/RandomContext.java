package fca.utils.readFile;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;

/**
 * 用于生成随机背景，输入背景中的对象数与属性数和背景名称
 * 生成背景在data文件夹中
 * 返回背景文件名称
 * */
public class RandomContext {
    public static String randomContext(int objSize, int attrSize, int m, String s) throws IOException {

        //String fileName = "src/BinaryContext/" + s + ".txt";
        String fileName = "src/lunWen2/" + s + ".txt";
        Path path = Paths.get(fileName);
        try (BufferedWriter writer =
                     Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(objSize + "," + attrSize + "\r\n");
        }
        int[][] array = new int[objSize][attrSize];
        Random random = new Random();
        random.setSeed(System.nanoTime());
        int count = objSize;
        for (int i = 0; i < objSize; i++) {
            int j = random.nextInt(attrSize);
            array[i][j] = 1;
        }
        for (int j = 0; j < attrSize; j++) {
            int i = random.nextInt(objSize);
            if (array[i][j] == 0) {
                array[i][j] = 1;
                count++;
            }
        }
        int onesCount =  objSize * attrSize * m /100 - count;
        System.out.println(onesCount);
        while (onesCount > 0) {
            int i = random.nextInt(objSize);
            int j = random.nextInt(attrSize);
            if (array[i][j] == 0) {
                array[i][j] = 1;
                onesCount--;
            }
        }
        try (BufferedWriter writer =
                     Files.newBufferedWriter(path,
                             StandardCharsets.UTF_8,
                             StandardOpenOption.APPEND)) {
            for (int i = 0; i < objSize; i++) {
                for (int j = 0; j < attrSize; j++) {
                    writer.write(array[i][j] + ",");
                }
                writer.write("\r\n");
            }
        }

        return fileName;
    }
}