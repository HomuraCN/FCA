package fca;

import fca.utils.Context;
import fca.utils.concept.AEConcept_id;
import fca.utils.readFile.AELatticeFileHandler;
import fca.utils.readFile.File;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static fca.algorithm.concept3c_ae.ICbo53C_ae_id.ICbo53C_ae_id_exe;
import static fca.utils.util.makeSet;

public class AEConceptTest {
    @Test
    void Cbo3c_aeTest() {
        try {
            long start_t = System.currentTimeMillis();
            Context context=File.readFile("src/main/java/data/context/test_deduplication.data.txt");
            int objs_size=context.getObjs_size();
            int attrs_size=context.getAttrs_size();
            AEConcept_id aeConcept_0=new AEConcept_id();
            aeConcept_0.setExtent(makeSet(0));
            aeConcept_0.setExtent_n(makeSet(0));
            aeConcept_0.setIntent(makeSet(attrs_size));
            aeConcept_0.setId(1);
            AEConcept_id aeConcept = new AEConcept_id();
            aeConcept.setExtent(makeSet(objs_size));
            aeConcept.setExtent_n(makeSet(objs_size));
            aeConcept.setIntent(makeSet(0));
            BitSet P=new BitSet();
            Queue<AEConcept_id> res=new LinkedList<>();
            res.add(aeConcept_0);
            ICbo53C_ae_id_exe(context,aeConcept,1,P,res);
            for(AEConcept_id node:res){
                System.out.println(node);
            }
            System.out.println(res.size());
            long end_t = System.currentTimeMillis();
            System.out.println("消耗时间为"+(end_t - start_t)+"ms");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void AELatticeFileHandlerTest() {
        try {
            ArrayList<AEConcept_id> aeConceptIds = AELatticeFileHandler.readAELatticeFromFile("src/main/java/data/lattice3C/test_deduplication_aelattice.data.txt");
            for(AEConcept_id aeConceptId:aeConceptIds){
                System.out.println(aeConceptId);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
