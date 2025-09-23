package fca.utils.dataProcess;

import fca.algorithm.concept3c_ae.ICbo53C_ae_id;
import fca.utils.Context;
import fca.utils.concept.AEConcept_id;
import fca.utils.readFile.AELatticeFileHandler;
import fca.utils.readFile.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.Queue;

import static fca.utils.util.makeSet;

/**
 * 将二进制形式背景文件转换为属性导出三支概念格文件
 */
public class BinaryContextToAELattice {
    public static void main(String[] args) {
        String fileName = "test_deduplication";
        String inputPath = "src/main/java/data/context/" + fileName + ".data.txt";
        String outputPath = "src/main/java/data/lattice3C/" + fileName + "_aelattice.data.txt";

        try {
            // 1. 读取形式背景
            System.out.println("Reading context from: " + inputPath);
            Context context = File.readFile(inputPath);

            // 2. 【已修正】严格按照 AEConceptTest.java 的方式初始化并调用算法
            System.out.println("Building Attribute-induced Three-way Concept Lattice using ICbo53C_ae_id...");

            // 初始化算法所需的参数
            int objs_size = context.getObjs_size();
            int attrs_size = context.getAttrs_size();

            // 创建初始概念 aeConcept_0 (对应测试代码中的顶概念)
            AEConcept_id aeConcept_0 = new AEConcept_id();
            aeConcept_0.setExtent(makeSet(0));
            aeConcept_0.setExtent_n(makeSet(0));
            aeConcept_0.setIntent(makeSet(attrs_size));
            aeConcept_0.setId(1);

            // 创建起始概念 aeConcept (对应测试代码中的底概念)
            AEConcept_id aeConcept = new AEConcept_id();
            aeConcept.setExtent(makeSet(objs_size));
            aeConcept.setExtent_n(makeSet(objs_size));
            aeConcept.setIntent(makeSet(0));

            // 初始化属性集P
            BitSet P = new BitSet();

            // 创建结果队列 res，并加入初始概念
            Queue<AEConcept_id> res = new LinkedList<>();
            res.add(aeConcept_0);

            // 使用所有五个参数调用算法
            ICbo53C_ae_id.ICbo53C_ae_id_exe(context, aeConcept, 1, P, res);

            // 将Queue转换为ArrayList以便写入文件
            ArrayList<AEConcept_id> aeLattice = new ArrayList<>(res);
            System.out.println("Lattice built with " + aeLattice.size() + " concepts.");

            // 3. 使用 AELatticeFileHandler 写入文件
            System.out.println("Writing AE lattice to: " + outputPath);
            AELatticeFileHandler.writeAELatticeToFile(aeLattice, outputPath);
            System.out.println("Conversion successful.");

        } catch (IOException e) {
            System.err.println("An error occurred during the conversion process.");
            e.printStackTrace();
        }
    }
}