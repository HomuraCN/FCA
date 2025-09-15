package fca.utils.readFile;

import java.io.File;
import java.util.Objects;
import java.util.Scanner;

public class ChooseFile {
    public static String chooseFile(String n) {
        File folder = new File(n); // 修改为你要扫描的文件夹路径
        File[] files = folder.listFiles(); // 获取文件夹中的所有文件
        // 输出文件列表供用户选择
        System.out.println("文件列表：");
        for (int i = 0; i < Objects.requireNonNull(files).length; i++) {
            if (files[i].isFile()) { // 判断是否为文件
                System.out.println((i + 1) + ". " + files[i].getName());
            }
        }

        // 获取用户选择的文件索引
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入要选择的文件序号：");
        int index = scanner.nextInt();

        // 根据用户选择的索引返回文件名
        if (index > 0 && index <= files.length && files[index - 1].isFile()) {
            System.out.println("您选择的文件名是：" + files[index - 1].getName());
            return files[index - 1].getName();
        } else {
            System.out.println("选择的文件序号无效！");
            return null;
        }
    }
}
