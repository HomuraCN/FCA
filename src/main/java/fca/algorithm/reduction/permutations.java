package fca.algorithm.reduction;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static fca.utils.util.is_subset_eq;


// 代表概念集 -> 组合约简
public class permutations {
    public static ArrayList<BitSet> permutations_exe(ArrayList<BitSet> concepts){
        //如果只有一个元素，说明结果就是该元素
        if(concepts.size()==1){
            return concepts;
        }
        BitSet node1=concepts.get(0);
        ArrayList<BitSet> list1=new ArrayList<>();
        for(int i= node1.nextSetBit(0);i>=0;i= node1.nextSetBit(i+1)){
            BitSet temp=new BitSet();
            temp.set(i);
            list1.add(temp);
        }
        for(int i=1;i<concepts.size();i++){
            BitSet node=concepts.get(i);
            list1=transformation(list1,node);
        }
        return list1;
    }

    private static ArrayList<BitSet> transformation(ArrayList<BitSet> list1,BitSet node){
        //R为结果集合
        ArrayList<BitSet> R=new ArrayList<>();
        Map<Integer,ArrayList<BitSet>> X=new HashMap<>();
        for(BitSet F:list1){
            for (int f= node.nextSetBit(0);f>=0;f= node.nextSetBit(f+1)){
                if(F.get(f)){
                    R.add(F);
                    BitSet x=(BitSet) F.clone();
                    x.set(f,false);
                    if(!X.containsKey(f)){
                        ArrayList<BitSet> t=new ArrayList<>();
                        t.add(x);
                        X.put(f,t);
                    }else{
                        X.get(f).add(x);
                    }
                }
            }
        }
        R=R.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
        for(BitSet F:list1){
            if(R.contains(F))continue;
            for(int f= node.nextSetBit(0);f>=0;f=node.nextSetBit(f+1)){
                if(!X.containsKey(f)||!inCloud(F,X.get(f))){
                    BitSet temp=(BitSet) F.clone();
                    temp.set(f);
                    R.add(temp);
                }
            }
        }
        return R;
    }

    private static boolean inCloud(BitSet f, ArrayList<BitSet> bitSets) {
        for(BitSet bitSet:bitSets){
            if(is_subset_eq(f,bitSet)){
                return true;
            }
        }
        return false;
    }
}
