package fca.algorithm.concept3c_ae;

import fca.utils.Context;
import fca.utils.concept.AEConcept;
import fca.utils.item.aeConceptItem;
import fca.utils.item.aeQueueItem;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.Queue;

import static fca.utils.util.*;

/**
 * 使用InClose5剪枝策略的三支概念格构造算法
 * */

public class PICbo53C_ae {
    public static void PICbo53C_ae_exe(Context context, AEConcept aeconcept, int v, BitSet P, int L, Queue<aeConceptItem> temp_queue, Queue<AEConcept> res){
        if(L==0){
            aeConceptItem item=new aeConceptItem(aeconcept,v,P);
            temp_queue.add(item);
            return;
        }
        Queue<aeQueueItem> queue=new LinkedList<>();
        int attrs_size=context.getAttrs_size(); 
        BitSet intent=aeconcept.getIntent();
        BitSet extent=aeconcept.getExtent();
        BitSet extent_n=aeconcept.getExtent_n();
        BitSet Q=(BitSet)P.clone();
        for(int j=v;j<=attrs_size;j++){
            if(!intent.get(j)&&!Q.get(j)){
                BitSet new_extent=intersection(extent,context.getAttrs().get(j));
                BitSet new_extent_n=intersection(extent_n,context.getAttrs_n().get(j));
                if(!new_extent.isEmpty()&&!new_extent_n.isEmpty()){      //核心概念需要把||换成&&
                    if(extent.equals(new_extent)&&extent_n.equals(new_extent_n)){
                        intent.set(j);
                    }else{
                        BitSet W_j=intersection(get_objs_shared(context,new_extent),get_vj(j));
                        BitSet Z_j=intersection(get_objs_not_shared(context,new_extent_n),get_vj(j));
                        BitSet M_j=intersection(W_j,Z_j);
                        BitSet I=intersection(intent,get_vj(j));
                        if(I.equals(M_j)){
                            queue.offer(new aeQueueItem(new_extent,new_extent_n,j));
                        }else {
                            if(difference(M_j,I).nextSetBit(0)<v){
                                Q.set(j);
                            }
                        }
                    }
                }else{
                    Q.set(j);
                }
            }
        }
        res.offer(aeconcept);
        System.out.println(aeconcept);
        while (!queue.isEmpty()){
            aeQueueItem item = queue.poll();
            BitSet new_intent=(BitSet)intent.clone();
            new_intent.set(item.getJ());
            AEConcept new_aeConcept=new AEConcept(item.getExtent(),item.getExtent_n(),new_intent);
            PICbo53C_ae_exe(context,new_aeConcept,item.getJ()+1,Q,L-1,temp_queue,res);
        }
    }
}
