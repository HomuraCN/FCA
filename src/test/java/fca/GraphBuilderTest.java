package fca;

import fca.algorithm.graph.GraphBuilder;
import fca.utils.Context;
import fca.utils.concept.AEConcept_id;
import fca.utils.concept.Concept;
import fca.utils.readFile.AELatticeFileHandler;
import fca.utils.readFile.File;
import fca.utils.readFile.LatticeFileHandler;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.ArrayList;

public class GraphBuilderTest {

    @Test
    public void testBuildAdjacencyMatrix() {
        try {
            // 1. 从文件加载形式背景，以获取对象总数
            Context context = File.readFile("src/main/java/data/context/iris.data.txt");
            int objectCount = context.getObjs_size();

            // 2. 从文件加载概念格
            ArrayList<Concept> concepts = LatticeFileHandler.readLatticeFromFile("src/main/java/data/lattice/iris_lattice.data.txt");
            
            // 3. 调用新方法构建邻接矩阵，并传入对象总数
            int[][] a_plus = GraphBuilder.buildAdjacencyMatrixDG(concepts, objectCount);

            // 4. (可选) 打印矩阵以验证结果
            System.out.println("邻接矩阵 A_plus 构建完成。");
            System.out.println("矩阵维度: " + a_plus.length + "x" + a_plus.length);

            // 打印部分矩阵内容进行抽查
            for (int i = 0; i < Math.min(10, a_plus.length); i++) {
                for (int j = 0; j < Math.min(10, a_plus.length); j++) {
                    System.out.print(a_plus[i][j] + "\t");
                }
                System.out.println();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testBuildAndWriteAdjacencyMatrix() {
        try {
            String datasetName = "test_deduplication"; // 方便切换数据集

            // 1. 加载形式背景
            Context context = File.readFile("src/main/java/data/context/" + datasetName + ".data.txt");
            int objectCount = context.getObjs_size();

            // 2. 加载概念格
            ArrayList<Concept> concepts = LatticeFileHandler.readLatticeFromFile("src/main/java/data/lattice/" + datasetName + "_lattice.data.txt");

            // 3. 构建邻接矩阵
            System.out.println("正在构建邻接矩阵 A_plus...");
            int[][] a_plus = GraphBuilder.buildAdjacencyMatrixDG(concepts, objectCount);
            System.out.println("构建完成。");

            // 4. 将矩阵写入文件
            String outputFilePath = "src/main/java/data/graph/" + datasetName + "_A_plus_DG.csv";
            GraphBuilder.writeMatrixToFile(a_plus, outputFilePath);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void testBuildAndWriteAEAdjacencyMatrix(){
        try {
            String datasetName = "test_deduplication"; // 方便切换数据集

            // 1. 加载形式背景
            Context context = File.readFile("src/main/java/data/context/" + datasetName + ".data.txt");
            int objectCount = context.getObjs_size();

            // 2. 加载概念格
            ArrayList<AEConcept_id> concepts = AELatticeFileHandler.readAELatticeFromFile("src/main/java/data/lattice3C/" + datasetName + "_aelattice.data.txt");

            // 3. 构建邻接矩阵
            System.out.println("正在构建邻接矩阵 A_negative...");
            int[][] a_negative = GraphBuilder.buildAEAdjacencyMatrixDG(concepts, objectCount);
            System.out.println("构建完成。");

            // 4. 将矩阵写入文件
            String outputFilePath = "src/main/java/data/graph/" + datasetName + "_A_negative_DG.csv";
            GraphBuilder.writeMatrixToFile(a_negative, outputFilePath);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}