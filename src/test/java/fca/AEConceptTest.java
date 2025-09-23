package fca;

import fca.utils.Context;
import fca.utils.concept.AEConcept;
import fca.utils.readFile.File;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static fca.algorithm.concept3c_ae.Cbo3c_ae.aeCbo3c_exe;
import static fca.utils.util.makeSet;

public class AEConceptTest {
    @Test
    void Cbo3c_aeTest() {
        try {
            //读入形式背景
            long start_t = System.currentTimeMillis();
            Context context = null;
            context = File.readFile("src/main/java/data/context/test_deduplication.data.txt");
            int objs_size=context.getObjs_size();
            AEConcept aeConcept = new AEConcept();
            aeConcept.setExtent(makeSet(objs_size));
            aeConcept.setExtent_n(makeSet(objs_size));
            aeConcept.setIntent(makeSet(0));
            Map<Integer, BitSet> nj=new HashMap<>();
            Queue<AEConcept> res=new LinkedList<>();
            aeCbo3c_exe(context,aeConcept,1,nj,false,res);
            for(AEConcept node:res){
                System.out.println(node);
            }
            System.out.println(res.size());
            long end_t = System.currentTimeMillis();
            System.out.println("消耗时间为"+(end_t - start_t)+"ms");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
