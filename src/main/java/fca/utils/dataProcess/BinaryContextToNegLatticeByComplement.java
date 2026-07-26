package fca.utils.dataProcess;

import fca.algorithm.concept.InClose3Iceberg;
import fca.utils.Context;
import fca.utils.concept.AEConcept_id;
import fca.utils.concept.Concept;
import fca.utils.readFile.AELatticeFileHandler;
import fca.utils.readFile.File;

import java.io.IOException;
import java.util.*;

import static fca.utils.util.*;

/**
 * 补背景法负概念格生成器。
 *
 * 理论依据（已在 car 数据集上验证）：
 *   负概念格 NL(K)（由负算子 A ↦ A∗̄, B ↦ B∗̄ 生成）恰好等于
 *   补背景 K^c = (O, M, (O×M)\I) 的经典概念格 L(K^c)。
 *   因此可直接对补背景运行正概念格算法（InClose3）得到全部负概念，
 *   无需构造三支 AE 概念格。
 *
 * 注意：这与生产管线中的"核心 AE 概念格"(正负外延均非空) 不是同一集合——
 *   核心 AE 额外要求内涵 B 满足 B* ≠ ∅（正向可实现），该约束会大幅缩减概念数。
 *   本类适用于需要完整负概念格或冰山负概念格的场景：
 *   minSupport > 0 时只保留负外延基数 >= minSupport 的负概念（冰山格）。
 *
 * 输出文件采用 AEConcept_R 格式以兼容既有下游 (extent = B*, extent_n = 负外延 Z, intent = B)。
 *
 * 用法: java ... BinaryContextToNegLatticeByComplement <dataset> [minSupport]
 */
public class BinaryContextToNegLatticeByComplement {

    /** 构造补背景：具有/不具有 关系互换（零拷贝，直接复用 Context 内部映射） */
    public static Context complement(Context c) {
        Context r = new Context();
        r.setObjs_size(c.getObjs_size());
        r.setAttrs_size(c.getAttrs_size());
        r.setObjs(c.getObjs_n());
        r.setObjs_n(c.getObjs());
        r.setAttrs(c.getAttrs_n());
        r.setAttrs_n(c.getAttrs());
        return r;
    }

    public static void main(String[] args) {
        String fileName = args.length > 0 ? args[0] : "test_deduplication";
        int minSupport = args.length > 1 ? Integer.parseInt(args[1]) : 0;
        String inputPath = "src/main/java/data/context/" + fileName + ".data.txt";
        String suffix = minSupport > 0 ? "_neglattice_iceberg" + minSupport : "_neglattice";
        String outputPath = "src/main/java/data/lattice3C/" + fileName + suffix + ".data.txt";

        try {
            System.out.println("Reading context from: " + inputPath);
            Context context = File.readFile(inputPath);
            Context comp = complement(context);
            int objs = context.getObjs_size();

            System.out.println("Building negative lattice via complement context + InClose3"
                    + (minSupport > 0 ? " (iceberg, minSupport=" + minSupport + ")" : "") + " ...");
            long t0 = System.currentTimeMillis();
            Concept init = new Concept();
            init.setExtent(makeSet(objs));
            init.setIntent(get_objs_shared(comp, init.getExtent()));
            Queue<Concept> negConcepts = new LinkedList<>();
            InClose3Iceberg.inClose3Iceberg_exe(comp, init, 1, new HashMap<>(), negConcepts,
                    minSupport, new int[]{1});
            System.out.println("Done: " + negConcepts.size() + " negative concepts, "
                    + (System.currentTimeMillis() - t0) + " ms");

            // 转换为 AEConcept_id 格式: extent = B*(原背景正外延), extent_n = Z, intent = B
            ArrayList<AEConcept_id> out = new ArrayList<>(negConcepts.size());
            int id = 1;
            for (Concept c : negConcepts) {
                BitSet posExtent = get_attrs_shared(context, c.getIntent());
                out.add(new AEConcept_id(id++, posExtent, c.getExtent(), c.getIntent()));
            }
            System.out.println("Writing negative lattice to: " + outputPath);
            AELatticeFileHandler.writeAELatticeToFile(out, outputPath);
            System.out.println("Conversion successful.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
