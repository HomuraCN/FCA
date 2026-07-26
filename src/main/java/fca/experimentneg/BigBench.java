package fca.experimentneg;

import fca.algorithm.concept3c_ae.ICbo53C_ae;
import fca.algorithm.concept3c_ae.ICbo53C_aeIceberg;
import fca.utils.Context;
import fca.utils.concept.AEConcept;
import fca.utils.readFile.File;

import java.util.*;

import static fca.utils.util.*;

/** 大数据集基准: 只计数不存储, 比较 core AE 基线与冰山剪枝耗时 */
public class BigBench {

    /** 只计数的队列, 避免在大数据集上占用内存 */
    static class CountingQueue<E> extends AbstractQueue<E> {
        long count = 0;
        public boolean offer(E e) { count++; return true; }
        public E poll() { return null; }
        public E peek() { return null; }
        public Iterator<E> iterator() { return Collections.emptyIterator(); }
        public int size() { return (int) Math.min(count, Integer.MAX_VALUE); }
    }

    public static void main(String[] args) throws Exception {
        String path = args[0];
        String mode = args[1]; // baseline | iceberg
        Context ctx = File.readFile(path);
        int objs = ctx.getObjs_size();
        System.out.println("context: " + objs + " objs, " + ctx.getAttrs_size() + " attrs, mode=" + mode);

        if (mode.equals("baseline")) {
            long t0 = System.currentTimeMillis();
            AEConcept bottom = new AEConcept(makeSet(objs), makeSet(objs), makeSet(0));
            CountingQueue<AEConcept> res = new CountingQueue<>();
            ICbo53C_ae.ICbo53C_ae_exe(ctx, bottom, 1, new BitSet(), res, true);
            System.out.println("core AE baseline: " + res.count + " concepts, "
                    + (System.currentTimeMillis() - t0) + " ms");
        } else {
            int sp = Integer.parseInt(args[2]);
            int sn = Integer.parseInt(args[3]);
            long t0 = System.currentTimeMillis();
            AEConcept bottom = new AEConcept(makeSet(objs), makeSet(objs), makeSet(0));
            CountingQueue<AEConcept> res = new CountingQueue<>();
            ICbo53C_aeIceberg.exe(ctx, bottom, 1, new BitSet(), res, sp, sn);
            System.out.println("iceberg core (sp=" + sp + ", sn=" + sn + "): " + res.count
                    + " concepts, " + (System.currentTimeMillis() - t0) + " ms");
        }
    }
}
