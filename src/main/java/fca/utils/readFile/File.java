package fca.utils.readFile;


import fca.utils.Context;

import java.io.FileReader;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class File {
    public static Context readFile(String filename)throws IOException {
        Context res=new Context();
        Scanner sc=new Scanner(new FileReader(filename));
        //每一行中元素以“，”分隔
        String[] s=sc.nextLine().split(",");
        //第一行中有两个数据，第一个为形式背景行数row，第二个为形式背景列数col
        int row=Integer.parseInt(s[0]);
        int col=Integer.parseInt(s[1]);
        res.setObjs_size(row);
        res.setAttrs_size(col);
        //objs为每一个对象具有的属性集合
        Map<Integer,BitSet> objs=new HashMap<>();
        //objs_n为每一个对象不具备的属性集合
        Map<Integer,BitSet> objs_n=new HashMap<>();
        //attrs为具有该属性的对象集合
        Map<Integer,BitSet> attrs=new HashMap<>();
        //attrs_n为不具有该属性的对象集合
        Map<Integer,BitSet> attrs_n=new HashMap<>();
        int[][] context=new int[row][col];
        //对象为行，属性为列
        for(int i=0;i<row;i++){
            String[] temp=sc.nextLine().split(",");
            BitSet obj=new BitSet();
            BitSet obj_n=new BitSet();
            for(int j=0;j<col;j++){
                context[i][j]=Integer.parseInt(temp[j]);
                if(context[i][j]==1){
                    obj.set(j+1);
                }else{
                    obj_n.set(j+1);
                }
            }
            objs.put(i+1,obj);
            objs_n.put(i+1,obj_n);
        }
        res.setObjs(objs);
        res.setObjs_n(objs_n);
        //转置形式背景中二元关系，对象变为列，属性变为行，
        int[][] context_t=transpose(row,col,context);
        for(int i=0;i<col;i++){
            BitSet attr=new BitSet();
            BitSet attr_n=new BitSet();
            for(int j=0;j<row;j++){
                if(context_t[i][j]==1){
                    attr.set(j+1);
                }else{
                    attr_n.set(j+1);
                }
            }
            attrs.put(i+1,attr);
            attrs_n.put(i+1,attr_n);
        }
        res.setAttrs(attrs);
        res.setAttrs_n(attrs_n);
        return res;
    }

    private static int[][] transpose(int row, int col, int[][] context) {
        int[][] res=new int[col][row];
        for(int i=0;i<row;i++){
            for(int j=0;j<col;j++){
                res[j][i]=context[i][j];
            }
        }
        return res;
    }
}
