package fca.experimentneg;

import fca.algorithm.concept3c_ae.ICbo53C_ae;
import fca.algorithm.concept3c_ae.ICbo53C_aeIceberg;
import fca.utils.Context;
import fca.utils.concept.AEConcept;
import fca.utils.readFile.File;

import java.util.*;

import static fca.utils.util.*;

/**
 * 验证: 冰山版核心AE算法输出 == 全量核心AE输出中满足支持度阈值的子集
 * 并测量剪枝带来的加速。
 */
public class IcebergCoreVerify {

    static Queue<AEConcept> runCore(Context ctx) {
        AEConcept bottom = new AEConcept(makeSet(ctx.getObjs_size()), makeSet(ctx.getObjs_size()), makeSet(0));
        Queue<AEConcept> res = new LinkedList<>();
        ICbo53C_ae.ICbo53C_ae_exe(ctx, bottom, 1, new BitSet(), res, true);
        return res;
    }

    static Queue<AEConcept> runIceberg(Context ctx, int sp, int sn) {
        AEConcept bottom = new AEConcept(makeSet(ctx.getObjs_size()), makeSet(ctx.getObjs_size()), makeSet(0));
        Queue<AEConcept> res = new LinkedList<>();
        ICbo53C_aeIceberg.exe(ctx, bottom, 1, new BitSet(), res, sp, sn);
        return res;
    }

    static String key(AEConcept c) {
        return c.getExtent() + "|" + c.getExtent_n() + "|" + c.getIntent();
    }

    public static void main(String[] args) throws Exception {
        String path = args.length > 0 ? args[0] : "data/car.data.txt";
        boolean doFull = args.length < 2 || !args[1].equals("nofull");
        Context ctx = File.readFile(path);
        int objs = ctx.getObjs_size();
        System.out.println("context: " + objs + " objs, " + ctx.getAttrs_size() + " attrs");

        Set<String> coreKeys = null;
        List<AEConcept> coreList = null;
        if (doFull) {
            long t0 = System.currentTimeMillis();
            Queue<AEConcept> core = runCore(ctx);
            System.out.println("core AE (baseline): " + core.size() + " concepts, "
                    + (System.currentTimeMillis() - t0) + " ms");
            coreList = new ArrayList<>(core);
            coreKeys = new HashSet<>();
            for (AEConcept c : core) coreKeys.add(key(c));
        }

        int[][] thresholds = {{1, 1}, {1, objs / 100}, {1, objs / 20}, {1, objs / 10},
                {objs / 100, objs / 100}, {objs / 100, objs / 10}};
        for (int[] th : thresholds) {
            int sp = Math.max(1, th[0]), sn = Math.max(1, th[1]);
            long t = System.currentTimeMillis();
            Queue<AEConcept> ice = runIceberg(ctx, sp, sn);
            long ms = System.currentTimeMillis() - t;
            String check = "";
            if (doFull) {
                Set<String> expect = new HashSet<>();
                for (AEConcept c : coreList) {
                    if (c.getExtent().cardinality() >= sp && c.getExtent_n().cardinality() >= sn) {
                        expect.add(key(c));
                    }
                }
                Set<String> got = new HashSet<>();
                for (AEConcept c : ice) got.add(key(c));
                check = ", 与全量过滤一致=" + got.equals(expect) + " (期望 " + expect.size() + ")";
            }
            System.out.println("iceberg core (minSupPos=" + sp + ", minSupNeg=" + sn + "): "
                    + ice.size() + " concepts, " + ms + " ms" + check);
        }
    }
}
