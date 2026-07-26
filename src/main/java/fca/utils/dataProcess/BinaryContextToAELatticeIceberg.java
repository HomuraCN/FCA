package fca.utils.dataProcess;

import fca.algorithm.concept3c_ae.ICbo53C_aeIceberg;
import fca.utils.Context;
import fca.utils.concept.AEConcept;
import fca.utils.concept.AEConcept_id;
import fca.utils.readFile.AELatticeFileHandler;
import fca.utils.readFile.File;

import java.io.IOException;
import java.util.*;

import static fca.utils.util.makeSet;

/**
 * 冰山版核心三支 AE 概念格生成器（生产语义兼容）。
 *
 * 与 BuildCategoricalExperimentInput.buildCoreNegativeLattice 生成的核心概念集
 * 语义一致（正负外延均非空），并额外支持两个支持度阈值：
 *   minSupPos: 正外延最小基数（=1 时与原核心算法一致）
 *   minSupNeg: 负外延最小基数（=1 时与原核心算法一致）
 * 已验证（car）：任意阈值下输出 == 原核心概念集中满足阈值的子集。
 *
 * 用法: java ... BinaryContextToAELatticeIceberg <dataset> [minSupPos] [minSupNeg]
 */
public class BinaryContextToAELatticeIceberg {
    public static void main(String[] args) {
        String fileName = args.length > 0 ? args[0] : "test_deduplication";
        int minSupPos = args.length > 1 ? Integer.parseInt(args[1]) : 1;
        int minSupNeg = args.length > 2 ? Integer.parseInt(args[2]) : 1;
        String inputPath = "src/main/java/data/context/" + fileName + ".data.txt";
        String outputPath = "src/main/java/data/lattice3C/" + fileName
                + "_aelattice_iceberg" + minSupPos + "_" + minSupNeg + ".data.txt";

        try {
            System.out.println("Reading context from: " + inputPath);
            Context context = File.readFile(inputPath);
            int objs = context.getObjs_size(), attrs = context.getAttrs_size();

            System.out.println("Building iceberg core AE lattice (minSupPos=" + minSupPos
                    + ", minSupNeg=" + minSupNeg + ") ...");
            long t0 = System.currentTimeMillis();
            AEConcept bottom = new AEConcept(makeSet(objs), makeSet(objs), makeSet(0));
            Queue<AEConcept> res = new LinkedList<>();
            ICbo53C_aeIceberg.exe(context, bottom, 1, new BitSet(), res, minSupPos, minSupNeg);
            System.out.println("Done: " + res.size() + " concepts, "
                    + (System.currentTimeMillis() - t0) + " ms");

            ArrayList<AEConcept_id> out = new ArrayList<>(res.size() + 1);
            int id = 1;
            // 与既有管线保持一致: 手动补顶概念 (∅, ∅, M)
            out.add(new AEConcept_id(id++, makeSet(0), makeSet(0), makeSet(attrs)));
            for (AEConcept c : res) {
                out.add(new AEConcept_id(id++, c.getExtent(), c.getExtent_n(), c.getIntent()));
            }
            System.out.println("Writing to: " + outputPath);
            AELatticeFileHandler.writeAELatticeToFile(out, outputPath);
            System.out.println("Conversion successful.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
