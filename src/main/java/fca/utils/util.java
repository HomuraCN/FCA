package fca.utils;

import java.io.IOException;
import java.util.BitSet;

public class util {


    /**
     * 由1..n-1构成的集合
     * @param n
     * @return
     */
    public static BitSet get_vj(int n){
        BitSet res=new BitSet();
        if(n==0) return res;
        res.set(1,n,true);
        return res;
    }

    /**
     * 由1..n构成的集合
     * @param n
     * @return
     */
    public static BitSet makeSet(int n){
        BitSet res=new BitSet();
        if(n==0) return res;
        res.set(1,n+1,true);
        return res;
    }

    /**
     * 集合交运算，并返回新集合
     * @param set1{1,2,3,4,5}
     * @param set2{3,4,6}
     * @return  res{3,4}
     */
    public static BitSet intersection(BitSet set1,BitSet set2){
        BitSet res=new BitSet();
        if(set1.isEmpty()||set2.isEmpty()){
            return res;
        }
        res=(BitSet)set1.clone();
        res.and(set2);
        return res;
    }


    /**
     * 集合是否包含
     * @param set1{1,2,3,4,5}
     * @param set2{1,2,3}
     * @return true
    */
    public static boolean is_subset_eq(BitSet set1, BitSet set2){
        if(set2==null)return true;
        if(set1.isEmpty())return false;
        for(int i=set2.nextSetBit(0);i>=0;i=set2.nextSetBit(i+1)){
            if(!set1.get(i)){
                return false;
            }
        }
        return true;
    }


    /**
     * 集合是否真包含
     * @param set1{1,2,3,4,5}
     * @param set2{1,2,3}
     * @return true
     */
    public static boolean is_subset(BitSet set1, BitSet set2){
        if (set1.equals(set2)){
            return false;
        }
        return is_subset_eq(set1,set2);
    }

    /**
     * 取多个对象中共同具有的属性集合，对应*算子
     * @param context 形式背景
     * @param objs  对象
     * @return
     */
    public static BitSet get_objs_shared(Context context,BitSet objs){
        if(objs.isEmpty()) return makeSet(context.getAttrs_size());
        BitSet res;
        res=(BitSet) context.getObjs().get(objs.nextSetBit(0)).clone();
        for(int i=objs.nextSetBit(0);i>=0;i=objs.nextSetBit(i+1)){
            res.and(context.getObjs().get(i));
        }
        return res;
    }

    /**
     * 取多个对象中共同不具有的属性，对应$\bar{*}$算子
     * @param context
     * @param objs
     * @return
     */
    public static BitSet get_objs_not_shared(Context context,BitSet objs){
        if(objs.isEmpty()) return makeSet(context.getAttrs_size());
        BitSet res;
        res=(BitSet) context.getObjs_n().get(objs.nextSetBit(0)).clone();
        for(int i=objs.nextSetBit(0);i>=0;i=objs.nextSetBit(i+1)){
            res.and(context.getObjs_n().get(i));
        }
        return res;
    }

    /**
     * 取共同拥有该属性集的对象集合，对应*算子
     * @param context
     * @param attrs
     * @return
     */
    public static BitSet get_attrs_shared(Context context,BitSet attrs){
        if(attrs.isEmpty()) return makeSet(context.getObjs_size());
        BitSet res;
        res=(BitSet) context.getAttrs().get(attrs.nextSetBit(0)).clone();
        for(int i=attrs.nextSetBit(0);i>=0;i=attrs.nextSetBit(i+1)){
            res.and(context.getAttrs().get(i));
        }
        return res;
    }

    /**
     * 取共同不具有该树形集的对象的集合，对应$\bar{*}$算子
     * @param context
     * @param attrs
     * @return
     */
    public static BitSet get_attrs_not_shared(Context context,BitSet attrs){
        if(attrs.isEmpty()) return makeSet(context.getObjs_size());
        BitSet res;
        res=(BitSet) context.getAttrs_n().get(attrs.nextSetBit(0)).clone();
        for(int i=attrs.nextSetBit(0);i>=0;i=attrs.nextSetBit(i+1)){
            res.and(context.getAttrs_n().get(i));
        }
        return res;
    }

    /**
     * 集合差运算，并返回集合
     * @param set1{1,2,3,4,5}
     * @param set2{3,4,6}
     * @return res{1,2,5}
     */
    public static BitSet difference(BitSet set1, BitSet set2){
        BitSet res=(BitSet) set1.clone();
        res.andNot(set2);
        return res;
    }

    /**
     * 集合并运算，并返回新集合
     * @param set1{1,2,5}
     * @param set2{3,4,6}
     * @return  res{1,2,3,4,5,6}
     */
    public static BitSet union(BitSet set1,BitSet set2){
            BitSet t=new BitSet();
        if(set1.isEmpty()&&set2.isEmpty()){
            return t;
        }else if(set1.isEmpty()){
            t=(BitSet) set2.clone();
            return t;
        }else if(set2.isEmpty()){
            t=(BitSet) set1.clone();
            return t;
        }else{
            t=(BitSet)set1.clone();
            t.or(set2);
            return t;
        }
    }

    public static void main(String[] args) throws IOException {

        BitSet b1 = new BitSet();
        BitSet b2 = new BitSet();
        BitSet b3 = new BitSet();
        b1.set(1);
        b1.set(2);
//        b1.set(3);
//        b1.set(4);
        b1.set(5);
        b2.set(3);
        b2.set(4);
        b2.set(6);
        System.out.println(union(b1,b2));
        System.out.println(b1);
        System.out.println(b2);

    }

}
