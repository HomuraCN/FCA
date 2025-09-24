package fca.utils.dataProcess;

import fca.algorithm.concept3c_ae.ICbo53C_ae_id;
import fca.utils.Context;
import fca.utils.concept.AEConcept;
import fca.utils.concept.AEConcept_id;
import fca.utils.item.aeConceptItem;
import fca.utils.readFile.AELatticeFileHandler;
import fca.utils.readFile.File;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static fca.algorithm.concept3c_ae.ICbo53C_ae.ICbo53C_ae_exe;
import static fca.algorithm.concept3c_ae.PICbo53C_ae.PICbo53C_ae_exe;
import static fca.utils.util.makeSet;

public class BinaryContextToAELatticeP {
    public static void main(String[] args) {
        String fileName = "iris";
        String inputPath = "src/main/java/data/context/" + fileName + ".data.txt";
        String outputPath = "src/main/java/data/lattice3C/" + fileName + "_aelattice.data.txt";

        try {
            // 1. 读取形式背景
            System.out.println("Reading context from: " + inputPath);
            Context context = File.readFile(inputPath);

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

            int id = 1;
            Queue<AEConcept_id> res_queue=new LinkedList<>();

            int aeConcept_num=res.size();
            for(AEConcept node:res){
                AEConcept_id aeConcept_id=new AEConcept_id(id++, node.getExtent(), node.getExtent_n(), node.getIntent());
                res_queue.add(aeConcept_id);
            }
            for(int i=0;i<n_threads;i++){
                aeConcept_num+=ress.get(i).size();
                for(AEConcept node:ress.get(i)){
                    AEConcept_id aeConcept_id=new AEConcept_id(id++, node.getExtent(), node.getExtent_n(), node.getIntent());
                    res_queue.add(aeConcept_id);
                }
            }
            System.out.println(aeConcept_num);

            // 将Queue转换为ArrayList以便写入文件
            ArrayList<AEConcept_id> aeLattice = new ArrayList<>(res_queue);
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

    private static void exe_demo(Context context, Queue<aeConceptItem> queue, Queue<AEConcept> res){
        if(queue.isEmpty()) return;
        for (aeConceptItem node:queue){
            ICbo53C_ae_exe(context,node.getAeConcept(),node.getJ(),node.getP(),res,true);
        }
    }
}
