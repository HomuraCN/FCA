package fca.algorithm.reduction;

import fca.utils.Context;
import fca.utils.concept.Concept;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static fca.utils.util.intersection;
import static fca.utils.util.is_subset;


// 求代表概念集
public class reduction_qis_Bit {
    public static ArrayList<BitSet> reduction_qis_exe_bit(Context context, Queue<Concept> concepts,int threads) {
        long start = System.currentTimeMillis();
        int objs_size=context.getObjs_size();
        int attr_size=context.getAttrs_size();
        Map<Integer, BitSet> concept_vec_by_obj=new HashMap<>();
        Map<Integer, BitSet> concept_vec_by_attr=new HashMap<>();
        ArrayList<BitSet> d_c = new ArrayList<>();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        Future<Map<Integer, BitSet>> obj =pool.submit(()->{
            Map<Integer, BitSet> res=new HashMap<>();
            for(int i=1;i<=objs_size;i++){
                BitSet temp=new BitSet();
                for(Concept node:concepts){
                    if(node.getExtent().get(i)){
                        temp.set(node.getId());
                    }
                }
                res.put(i,temp);
            }
            return res;
        });
        Future<Map<Integer, BitSet>> attr=pool.submit(()->{
            Map<Integer, BitSet> res=new HashMap<>();
            for(int i=1;i<=attr_size;i++){
                BitSet temp=new BitSet();
                for(Concept node:concepts){
                    if(node.getIntent().get(i)){
                        temp.set(node.getId());
                    }
                }
                res.put(i,temp);
            }
            return res;
        });
        try {
            concept_vec_by_obj=obj.get();
            concept_vec_by_attr=attr.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Map<Integer,Set<Integer>> map=new HashMap<>();
        for(int i=0;i<threads;i++){
            map.put(i,new HashSet<>());
        }
        for(int i=1;i<=objs_size;i++){
            int num=i%threads;
            map.get(num).add(i);

        }
        Map<Integer,BitSet> finalConcept_vec_by_attr=concept_vec_by_attr;
        Map<Integer,BitSet> finalConcept_vec_by_obj=concept_vec_by_obj;
        Map<Integer,ArrayList<BitSet>> d_c_temp=new HashMap<>();
        Map<Integer,Future<?>> thread_map=new HashMap<>();
        for(int i=0;i<threads;i++){
            int finalI = i;
            d_c_temp.put(finalI,new ArrayList<>());
            thread_map.put(i,pool.submit(()->{
                for(Integer num:map.get(finalI)){
                    for(int j=1;j<=attr_size;j++){
                        BitSet temp=intersection(finalConcept_vec_by_obj.get(num),finalConcept_vec_by_attr.get(j));
                        if(!temp.isEmpty()){
                            d_c_temp.get(finalI).add(temp);
                        }
                    }
                }
            }));
        }
        for(int i=0;i<threads;i++){
            try {
                thread_map.get(i).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        pool.shutdown();
        for(int i=0;i<threads;i++){
            d_c.addAll(d_c_temp.get(i));
        }

        long end = System.currentTimeMillis();
        System.out.println("MTCR 第一步的时间："+(end-start));
        long start_2 = System.currentTimeMillis();
        ArrayList<BitSet> res_temp=get_minimal(d_c);
        long end_2 = System.currentTimeMillis();
        System.out.println("MTCR 第二步的时间："+(end_2-start_2));
        return new ArrayList<>(res_temp);

    }

    private static ArrayList<BitSet> get_minimal(ArrayList<BitSet> concepts){
        concepts.sort(Comparator.comparingInt(BitSet::cardinality));
        int i=0;
        while(i<concepts.size()){
            int j=i+1;
            while (j<concepts.size()){
                if(is_subset(concepts.get(j),concepts.get(i))) {
                    concepts.remove(j);
                    j--;
                }
                j++;
            }
            i++;
        }
        return concepts;
    }

}
