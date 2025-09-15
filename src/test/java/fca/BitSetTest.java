package fca;

import fca.utils.readFile.BitSetFileHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;

public class BitSetTest {
    @Test
    void testBitSet(){
        // 假设有5门课: 0=数学, 1=物理, 2=化学, 3=历史, 4=英语
        final int MATH = 0;
        final int PHYSICS = 1;
        final int CHEMISTRY = 2;
        final int HISTORY = 3;
        final int ENGLISH = 4;

        // 创建一个 ArrayList 来存储每个学生的选课情况
        ArrayList<BitSet> studentCourses = new ArrayList<>();

        // === 添加学生数据 ===
        // 学生0 (Alice) 选了 数学, 化学, 英语
        BitSet aliceCourses = new BitSet(5);
        aliceCourses.set(MATH);
        aliceCourses.set(CHEMISTRY);
        aliceCourses.set(ENGLISH);
        studentCourses.add(aliceCourses);

        // 学生1 (Bob) 选了 数学, 物理
        BitSet bobCourses = new BitSet(5);
        bobCourses.set(MATH);
        bobCourses.set(PHYSICS);
        studentCourses.add(bobCourses);

        // 学生2 (Charlie) 选了 物理, 历史, 英语
        BitSet charlieCourses = new BitSet(5);
        charlieCourses.set(PHYSICS);
        charlieCourses.set(HISTORY);
        charlieCourses.set(ENGLISH);
        studentCourses.add(charlieCourses);

        // === 查询数据 ===
        System.out.println("Bob是否选了物理? " + studentCourses.get(1).get(PHYSICS)); // true
        System.out.println("Alice是否选了物理? " + studentCourses.get(0).get(PHYSICS)); // false

        // === 使用位运算 ===
        // 找出Alice和Bob共同选择的课程 (交集)
        BitSet commonCourses = (BitSet) aliceCourses.clone(); // 克隆一个副本进行操作，避免修改原数据
        commonCourses.and(bobCourses); // 执行 AND 操作
        System.out.println("Alice和Bob的共同课程: " + commonCourses); // {0} (代表数学)

        // 找出Alice或Charlie选择的所有课程 (并集)
        BitSet unionCourses = (BitSet) aliceCourses.clone();
        unionCourses.or(charlieCourses); // 执行 OR 操作
        System.out.println("Alice或Charlie选择的所有课程: " + unionCourses); // {0, 2, 3, 4} (数学, 化学, 历史, 英语)

        // 遍历打印某个学生的所有选课
        System.out.print("Charlie选的所有课程ID: ");
        charlieCourses.stream().forEach(courseId -> System.out.print(courseId + " ")); // 输出: 1 3 4
        System.out.println();

        for(int i = 0; i < studentCourses.size(); i++){
            System.out.println("学生 " + (i + 1) + ": " + studentCourses.get(i));
        }
        System.out.println(charlieCourses);
    }

    @Test
    void testBitSetFileHandler(){
        try {
            ArrayList<BitSet> bitSetArrayList = BitSetFileHandler.readFromFile("src/main/java/data/reduction/car_reduction.data.txt");
            System.out.println(bitSetArrayList.size());
            System.out.println(bitSetArrayList.get(0));
            System.out.println(bitSetArrayList.get(0).size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
