package fca.algorithm.reduction;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static fca.utils.util.difference;
import static fca.utils.util.is_subset_eq;

public class permutations {
    public static ArrayList<BitSet> permutations_exe(ArrayList<BitSet> concepts){
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
        return list1.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
    }

    private static ArrayList<BitSet> transformation(ArrayList<BitSet> list1,BitSet node){
        ArrayList<BitSet> R=new ArrayList<>();
        Map<Integer,BitSet> X=new HashMap<>();
        for(BitSet F:list1){
            for(int num=node.nextSetBit(0);num>=0;num= node.nextSetBit(num+1)){
                if(F.get(num)){
                    R.add(F);
                    BitSet temp=new BitSet();
                    temp.set(num);
                    BitSet x=difference(F,temp);
                    X.put(num,x);
                }
            }
        }
        for(BitSet F:list1){
            if(R.contains(F))continue;
            for(int num= node.nextSetBit(0);num>=0;num=node.nextSetBit(num+1)){
                if(!X.containsKey(num)||!is_subset_eq(F,X.get(num))){
                    BitSet temp=(BitSet) F.clone();
                    temp.set(num);
                    R.add(temp);
                }
            }
        }
        return R;
    }
}
