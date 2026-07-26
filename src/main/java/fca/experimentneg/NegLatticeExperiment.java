package fca.experimentneg;

import fca.algorithm.concept.InClose3;
import fca.algorithm.concept3c_ae.ICbo53C_ae_id;
import fca.utils.Context;
import fca.utils.concept.AEConcept_id;
import fca.utils.concept.Concept;
import fca.utils.readFile.File;

import java.util.*;

import static fca.utils.util.*;

/**
 * 实验：验证 "补背景 + 正概念格算法(InClose3)" 是否能替代
 * "属性导出三支概念格(ICbo53C_ae)" 来生成负概念格。
 *
 * 理论依据：
 *   负概念格 NL(K) := 由负算子 (A ↦ A∗̄, B ↦ B∗̄) 生成的概念格
 *   恰好等于补背景 K^c=(O, M, (O×M)\I) 的经典概念格 L(K^c)。
 *   而 AE 三支概念的负外延 Ext_n(c) = Int(c)∗̄ 都是 K^c 中的外延，
 *   因此 { Ext_n(c) } (去重) 应当等于 L(K^c) 的外延集合。
 */
public class NegLatticeExperiment {

    /** 构造补背景：具有/不具有 关系互换 */
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

    public static void main(String[] args) throws Exception {
        String path = args.length > 0 ? args[0] : "data/car.data.txt";
        Context context = File.readFile(path);
        int objs = context.getObjs_size(), attrs = context.getAttrs_size();
        System.out.println("context: " + objs + " objs, " + attrs + " attrs");

        // ---------- 方案A: 现行三支 AE 概念格 ----------
        long t0 = System.currentTimeMillis();
        AEConcept_id top = new AEConcept_id();
        top.setExtent(makeSet(0));
        top.setExtent_n(makeSet(0));
        top.setIntent(makeSet(attrs));
        top.setId(1);
        AEConcept_id bottom = new AEConcept_id();
        bottom.setExtent(makeSet(objs));
        bottom.setExtent_n(makeSet(objs));
        bottom.setIntent(makeSet(0));
        Queue<AEConcept_id> aeRes = new LinkedList<>();
        aeRes.add(top);
        ICbo53C_ae_id.ICbo53C_ae_id_exe(context, bottom, 1, new BitSet(), aeRes);
        long tAE = System.currentTimeMillis() - t0;
        System.out.println("[A] AE 3C lattice: " + aeRes.size() + " concepts, " + tAE + " ms");

        // AE 概念的负外延去重
        HashSet<BitSet> aeNegExtents = new HashSet<>();
        // 负外延 -> 最大内涵（并集近似检查用）
        HashMap<BitSet, BitSet> aeMaxIntent = new HashMap<>();
        for (AEConcept_id c : aeRes) {
            BitSet en = c.getExtent_n();
            aeNegExtents.add(en);
            BitSet acc = aeMaxIntent.get(en);
            if (acc == null) aeMaxIntent.put(en, (BitSet) c.getIntent().clone());
            else acc.or(c.getIntent());
        }
        System.out.println("[A] distinct negative extents (Ext_n): " + aeNegExtents.size());

        // ---------- 方案B: 补背景 + InClose3 ----------
        long t1 = System.currentTimeMillis();
        Context comp = complement(context);
        Concept init = new Concept();
        init.setExtent(makeSet(objs));
        init.setIntent(get_objs_shared(comp, init.getExtent()));
        Queue<Concept> negRes = new LinkedList<>();
        InClose3.inClose3_exe(comp, init, 1, new HashMap<>(), negRes);
        long tNeg = System.currentTimeMillis() - t1;
        System.out.println("[B] complement-context lattice (InClose3): " + negRes.size()
                + " concepts, " + tNeg + " ms");

        // ---------- 验证 ----------
        HashSet<BitSet> negExtents = new HashSet<>();
        HashMap<BitSet, BitSet> negIntent = new HashMap<>();
        for (Concept c : negRes) {
            negExtents.add(c.getExtent());
            negIntent.put(c.getExtent(), c.getIntent());
        }
        System.out.println("[B] distinct extents: " + negExtents.size());

        // InClose3 从全对象顶点出发，可能不包含空外延顶概念；比较时对齐处理
        HashSet<BitSet> aeAdj = new HashSet<>(aeNegExtents);
        HashSet<BitSet> negAdj = new HashSet<>(negExtents);

        boolean equal = aeAdj.equals(negAdj);
        System.out.println("Ext_n(AE) 集合 == L(K^c) 外延集合 ? " + equal);
        if (!equal) {
            HashSet<BitSet> onlyAE = new HashSet<>(aeAdj); onlyAE.removeAll(negAdj);
            HashSet<BitSet> onlyNeg = new HashSet<>(negAdj); onlyNeg.removeAll(aeAdj);
            System.out.println("  only in AE:  " + onlyAE.size());
            System.out.println("  only in Neg: " + onlyNeg.size());
            int shown = 0;
            for (BitSet b : onlyAE) { System.out.println("   AE-only ext_n: " + b); if (++shown >= 3) break; }
            shown = 0;
            for (BitSet b : onlyNeg) { System.out.println("   Neg-only ext: " + b); if (++shown >= 3) break; }
        }

        // 内涵关系检查：L(K^c) 中 (Z,B) 的 B 应为该负外延对应的最大内涵，
        // 且等于所有共享该 Ext_n 的 AE 概念内涵的并集的负闭包
        int intentMatch = 0, intentDiffer = 0;
        for (Map.Entry<BitSet, BitSet> e : negIntent.entrySet()) {
            BitSet unionAE = aeMaxIntent.get(e.getKey());
            if (unionAE != null && unionAE.equals(e.getValue())) intentMatch++;
            else intentDiffer++;
        }
        System.out.println("内涵检查: L(K^c) 内涵 == 同负外延 AE 内涵之并: match=" + intentMatch
                + ", differ=" + intentDiffer);
    }
}
