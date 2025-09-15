package fca.algorithm.concept;



import fca.utils.Context;
import fca.utils.concept.Concept;
import fca.utils.item.queueItem_InClose;

import java.util.*;

import static fca.utils.util.*;

// 建格算法
public class InClose3 {
    private static int id=1;
    public static void inClose3_exe(Context context, Concept concept, int v,
                                    Map<Integer, BitSet> nj, Queue<Concept> res){
        Queue<queueItem_InClose> queue=new LinkedList<>();
        Map<Integer,BitSet> mj=new HashMap<>();
        int attr_size=context.getAttrs_size();
        for(int j=v;j<=attr_size;j++){
            BitSet intent=concept.getIntent();
            BitSet extent=concept.getExtent();
            //Mj <- Nj
            mj.put(j,nj.get(j));
            //j 不属于 A and Nj属于A∩Vj
            if((intent.isEmpty()||!intent.get(j))&&is_subset_eq(intersection(intent,get_vj(j)),nj.get(j))){
                //W <- X ∩ {j}*
                BitSet W=intersection(extent,context.getAttrs().get(j));
                //System.out.println(W.toString());
                //if X=W then
                if(extent.equals(W)){
                    // A <- A ∪ {j}
                    intent.set(j);
                    concept.setIntent(intent);
                }else{
                    // A ∩ Vj
                    BitSet A_Vj=intersection(intent,get_vj(j));
                    // W*j
                    BitSet W_j=intersection(get_objs_shared(context,W),get_vj(j));
                    // if(A ∩ Vj = W*j)then
                    if(A_Vj.equals(W_j)){
                        //PutInQueue(W，j)
                        queueItem_InClose newItem=new queueItem_InClose(W,j);
                        queue.offer(newItem);
                    }else{
                        // Mj <- W*j
                        mj.put(j,W_j);
                    }
                }
            }

        }
        Concept concept_temp = new Concept(concept.getExtent(), concept.getIntent(), id);
        id++;
        res.offer(concept_temp);

        //while GetFromQueue(W，j) do
        while (!queue.isEmpty()){
            queueItem_InClose item=queue.poll();
            //B<-A∪{j}
            BitSet B=(BitSet)concept.getIntent().clone() ;
            B.set(item.getJ());
            Concept newConcept=new Concept(item.getW(),B);
            //InClose3 ((W，B)，j+1，{ Mv|v∈V})
            inClose3_exe(context,newConcept,item.getJ()+1,mj,res);
        }

    }
}
