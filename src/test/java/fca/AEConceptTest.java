package fca;

import fca.utils.Context;
import fca.utils.concept.AEConcept;
import fca.utils.concept.AEConcept_id;
import fca.utils.item.aeConceptItem;
import fca.utils.readFile.AELatticeFileHandler;
import fca.utils.readFile.File;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static fca.algorithm.concept3c_ae.ICbo53C_ae.ICbo53C_ae_exe;
import static fca.algorithm.concept3c_ae.ICbo53C_ae_id.ICbo53C_ae_id_exe;
import static fca.algorithm.concept3c_ae.PICbo53C_ae.PICbo53C_ae_exe;
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
    @Test
    void PICbo53c_aeTest(){
        try {
            long start_t = System.currentTimeMillis();
            //读入形式背景
            Context context=File.readFile("src/main/java/data/context/test_deduplication.data.txt");
            int n_threads=4;
            int x=3;
            int objs_size=context.getObjs_size();
            int attrs_size=context.getAttrs_size();
            AEConcept aeConcept_0=new AEConcept();
            aeConcept_0.setExtent(makeSet(0));
            aeConcept_0.setExtent_n(makeSet(0));
            aeConcept_0.setIntent(makeSet(attrs_size));
            AEConcept aeConcept = new AEConcept();
            aeConcept.setExtent(makeSet(objs_size));
            aeConcept.setExtent_n(makeSet(objs_size));
            aeConcept.setIntent(makeSet(0));
            BitSet P=new BitSet();
            Queue<AEConcept> res=new LinkedList<>();
            Queue<aeConceptItem> temp_queue=new LinkedList<>();
            res.add(aeConcept_0);
            PICbo53C_ae_exe(context,aeConcept,1,P,x,temp_queue,res);
            Map<Integer,Queue<aeConceptItem>> queues=new HashMap<>();
            for(int i=0;i<n_threads;i++){
                queues.put(i,new LinkedList<>());
            }
            int n=temp_queue.size();
            for(int i=0;i<n;i++){
                queues.get(i%n_threads).offer(temp_queue.poll());
            }
            Map<Integer,Queue<AEConcept>> ress=new HashMap<>();
            for(int i=0;i<n_threads;i++){
                ress.put(i,new LinkedList<>());
            }
            ExecutorService pool = Executors.newFixedThreadPool(n_threads);
            Map<Integer, Future<?>> map=new HashMap<>();
            for(int i=0;i<n_threads;i++){
                int finalI=i;
                map.put(i,pool.submit(()->exe_demo(context,queues.get(finalI),ress.get(finalI))));
            }
            for(Integer node:map.keySet()){
                try {
                    map.get(node).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            pool.shutdown();
            int aeConcept_num=res.size();
//        for(AEConcept node:res){
//            System.out.println(node);
//        }
            for(int i=0;i<n_threads;i++){
                aeConcept_num+=ress.get(i).size();
//            for(AEConcept node:ress.get(i)){
//                System.out.println(node);
//            }
            }
            System.out.println(aeConcept_num);

            long end_t = System.currentTimeMillis();
            System.out.println("消耗时间为"+(end_t - start_t)+"ms");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void exe_demo(Context context,Queue<aeConceptItem> queue,Queue<AEConcept> res){
        if(queue.isEmpty()) return;
        for (aeConceptItem node:queue){
            ICbo53C_ae_exe(context,node.getAeConcept(),node.getJ(),node.getP(),res,true);
        }
    }
}
