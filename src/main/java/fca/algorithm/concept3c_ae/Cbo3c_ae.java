package fca.algorithm.concept3c_ae;

import fca.utils.Context;
import fca.utils.concept.AEConcept;
import fca.utils.item.aeQueueItem;



import java.util.*;

import static fca.utils.util.*;


public class Cbo3c_ae {
    static  Integer count=1;
    public static void aeCbo3c_exe(Context context, AEConcept aeConcept, int v,
                                   Map<Integer,BitSet> nj,
                                   boolean core, Queue<AEConcept> res){
        Queue<aeQueueItem> queue=new LinkedList<>();
        Map<Integer,BitSet> mj=new HashMap<>();
        // for j<-v upto n-1 do
        int objs_size=context.getObjs_size();
        int attrs_size=context.getAttrs_size();
        for(int j=v;j<=attrs_size;j++){
            BitSet intent=aeConcept.getIntent();
            BitSet extent=aeConcept.getExtent();
            BitSet extent_n=aeConcept.getExtent_n();
            //Mj <- Nj
            mj.put(j,nj.get(j));
            //j 不属于 A and Nj属于A∩Vj
            if(!intent.get(j)&&is_subset_eq(intersection(intent,get_vj(j)),nj.get(j))){
                //W <- X ∩ {j}*
                BitSet W=intersection(extent,context.getAttrs().get(j));
                //if Core=true and W=∅
                if(core&&W.isEmpty()){ continue; }
                //Z <- Y ∩ {j}-*
                BitSet Z=intersection(extent_n,context.getAttrs_n().get(j));
                //if Core = true and Z = ∅
                if(core&&Z.isEmpty()){
                    continue;
                }
                if(extent.equals(W)&&extent_n.equals(Z)){
                    intent.set(j);
                    aeConcept.setIntent(intent);
                }else {
                    BitSet W_j=intersection(get_objs_shared(context,W),get_vj(j));
                    BitSet Z_j=intersection(get_objs_not_shared(context,Z),get_vj(j));
                    BitSet M_j=intersection(W_j,Z_j);
                    if(intersection(intent,get_vj(j)).equals(M_j)){
                        aeQueueItem item=new aeQueueItem(W,Z,j);
                        queue.offer(item);
                    }else{
                        mj.put(j,M_j);
                    }
                }
            }
        }

        //处理已经计算出的三支概念
        res.offer(aeConcept);
        //System.out.println(count++);

        //计算队列中的概念
        while (!queue.isEmpty()){
            aeQueueItem item=queue.poll();
            AEConcept next_aeConcept=new AEConcept();
            next_aeConcept.setExtent(item.getExtent());
            next_aeConcept.setExtent_n(item.getExtent_n());
            BitSet new_intent;
            new_intent=(BitSet)aeConcept.getIntent().clone();
            int j=item.getJ();
            new_intent.set(j);
            next_aeConcept.setIntent(new_intent);
            aeCbo3c_exe(context,next_aeConcept,j+1,mj,core,res);
        }
    }
}
