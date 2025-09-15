package fca.utils.readFile;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class UCIContext {
    public static String UciContext(int objSize,int attrSize,boolean[][] data,String name) throws IOException {
        String fileName="src/main/java/data/context/"+name+".txt";
        Path path= Paths.get(fileName);
        try (BufferedWriter writer =
                     Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(objSize+","+attrSize);
        }
        try (BufferedWriter writer =
                     Files.newBufferedWriter(path,
                             StandardCharsets.UTF_8,
                             StandardOpenOption.APPEND)){
            writer.write("\r\n");
        }
        for(int i=0;i<objSize;i++){
            for(int j=0;j<attrSize;j++){
                int num=0;
                if(data[i][j]){
                    num=1;
                }
                try (BufferedWriter writer =
                             Files.newBufferedWriter(path,
                                     StandardCharsets.UTF_8,
                                     StandardOpenOption.APPEND)){
                    writer.write(num+",");
                }
            }
            try (BufferedWriter writer =
                         Files.newBufferedWriter(path,
                                 StandardCharsets.UTF_8,
                                 StandardOpenOption.APPEND)){
                writer.write("\r\n");
            }
        }
        return fileName;
    }
}
