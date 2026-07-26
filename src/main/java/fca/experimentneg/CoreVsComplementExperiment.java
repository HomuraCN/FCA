package fca.experimentneg;

import fca.algorithm.concept.InClose3;
import fca.algorithm.concept.InClose3Iceberg;
import fca.algorithm.concept3c_ae.ICbo53C_ae;
import fca.utils.Context;
import fca.utils.concept.AEConcept;
import fca.utils.concept.Concept;
import fca.utils.readFile.File;

import java.util.*;

import static fca.utils.util.*;

/**
 * 对比实验：
 *   [A] 现行生产管线的"核心"三支AE概念格 (extent、extent_n 均非空, ICbo53C_ae core=true)
 *   [B] 补背景 + InClose3 (完整负概念格)
 *   [C] 补背景 + InClose3Iceberg (冰山负概念格, 不同 minSupport)
 * 并分析核心AE集合与补背景概念格之间的关系。
 */
public class CoreVsComplementExperiment {

    public static void main(String[] args) throws Exception {
        String path = args.length > 0 ? args[0] : "data/car.data.txt";
        Context context = File.readFile(path);
        int objs = context.getObjs_size(), attrs = context.getAttrs_size();
        System.out.println("context: " + objs + " objs, " + attrs + " attrs");

        // ---------- [A] 核心 AE 概念格 (现行生产语义, 单线程) ----------
        long t0 = System.currentTimeMillis();
        AEConcept top = new AEConcept(makeSet(0), makeSet(0), makeSet(attrs));
        AEConcept bottom = new AEConcept(makeSet(objs), makeSet(objs), makeSet(0));
        Queue<AEConcept> core = new LinkedList<>();
        core.add(top);
        ICbo53C_ae.ICbo53C_ae_exe(context, bottom, 1, new BitSet(), core, true);
        long tCore = System.currentTimeMillis() - t0;
        System.out.println("[A] core AE lattice: " + core.size() + " concepts, " + tCore + " ms");

        // 核心概念的负外延统计
        HashSet<BitSet> coreNegExtents = new HashSet<>();
        HashMap<BitSet, Integer> extNCount = new HashMap<>();
        for (AEConcept c : core) {
            coreNegExtents.add(c.getExtent_n());
            extNCount.merge(c.getExtent_n(), 1, Integer::sum);
        }
        int dup = 0;
        for (int v : extNCount.values()) if (v > 1) dup++;
        System.out.println("[A] distinct Ext_n among core: " + coreNegExtents.size()
                + " (有重复Ext_n的组数: " + dup + ")");

        // ---------- [B] 补背景 + InClose3 完整负格 ----------
        long t1 = System.currentTimeMillis();
        Context comp = NegLatticeExperiment.complement(context);
        Concept init = new Concept();
        init.setExtent(makeSet(objs));
        init.setIntent(get_objs_shared(comp, init.getExtent()));
        Queue<Concept> full = new LinkedList<>();
        InClose3.inClose3_exe(comp, init, 1, new HashMap<>(), full);
        long tFull = System.currentTimeMillis() - t1;
        System.out.println("[B] full complement lattice: " + full.size() + " concepts, " + tFull + " ms");

        // 核心 Ext_n 是否都是补背景概念的外延?
        HashSet<BitSet> fullExtents = new HashSet<>();
        for (Concept c : full) fullExtents.add(c.getExtent());
        int inFull = 0;
        for (BitSet en : coreNegExtents) if (fullExtents.contains(en)) inFull++;
        System.out.println("    core Ext_n ⊆ L(K^c) extents ? " + inFull + "/" + coreNegExtents.size());

        // ---------- [C] 补背景 + 冰山格, 多个支持度阈值 ----------
        int[] minsups = {objs / 2, objs / 4, objs / 10, objs / 20, objs / 50, objs / 100};
        for (int ms : minsups) {
            long t2 = System.currentTimeMillis();
            Concept init2 = new Concept();
            init2.setExtent(makeSet(objs));
            init2.setIntent(get_objs_shared(comp, init2.getExtent()));
            Queue<Concept> ice = new LinkedList<>();
            InClose3Iceberg.inClose3Iceberg_exe(comp, init2, 1, new HashMap<>(), ice, ms, new int[]{1});
            long tIce = System.currentTimeMillis() - t2;

            // 覆盖率: 每个对象至少属于一个冰山概念的比例 & core Ext_n 命中率
            BitSet covered = new BitSet();
            HashSet<BitSet> iceExtents = new HashSet<>();
            for (Concept c : ice) { covered.or(c.getExtent()); iceExtents.add(c.getExtent()); }
            int coreHit = 0;
            for (BitSet en : coreNegExtents) if (iceExtents.contains(en)) coreHit++;
            System.out.println("[C] iceberg minsup=" + ms + " (" + String.format("%.1f", 100.0 * ms / objs)
                    + "%): " + ice.size() + " concepts, " + tIce + " ms, obj-coverage=" + covered.cardinality()
                    + "/" + objs + ", 命中core负外延=" + coreHit + "/" + coreNegExtents.size());
        }

        // ---------- 冰山格用于正概念格 ----------
        System.out.println();
        long t3 = System.currentTimeMillis();
        Concept initP = new Concept();
        initP.setExtent(makeSet(objs));
        initP.setIntent(get_objs_shared(context, initP.getExtent()));
        Queue<Concept> pos = new LinkedList<>();
        InClose3.inClose3_exe(context, initP, 1, new HashMap<>(), pos);
        System.out.println("[P] positive lattice: " + pos.size() + " concepts, "
                + (System.currentTimeMillis() - t3) + " ms");
        for (int ms : new int[]{objs / 10, objs / 50, objs / 100}) {
            long t4 = System.currentTimeMillis();
            Concept initI = new Concept();
            initI.setExtent(makeSet(objs));
            initI.setIntent(get_objs_shared(context, initI.getExtent()));
            Queue<Concept> iceP = new LinkedList<>();
            InClose3Iceberg.inClose3Iceberg_exe(context, initI, 1, new HashMap<>(), iceP, ms, new int[]{1});
            System.out.println("[P] iceberg minsup=" + ms + ": " + iceP.size() + " concepts, "
                    + (System.currentTimeMillis() - t4) + " ms");
        }
    }
}
