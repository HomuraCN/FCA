package fca.utils.readFile;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class GetDensity {
    public static void main(String[] args) throws FileNotFoundException {
        String filename="src/main/java/data/context/mushroom.data.txt";
        Scanner sc=new Scanner(new FileReader(filename));
        String[] s=sc.nextLine().split(",");
        int row=Integer.parseInt(s[0]);
        int col=Integer.parseInt(s[1]);
        int count=0;
        int[][] context=new int[row][col];
        for(int i=0;i<row;i++){
            String[] temp=sc.nextLine().split(",");
            for(int j=0;j<col;j++){
                context[i][j]=Integer.parseInt(temp[j]);
                if(context[i][j]==1){
                    count++;
                }
            }
        }
        float density=(float) count/(row*col);
        System.out.println(density);
    }
}
