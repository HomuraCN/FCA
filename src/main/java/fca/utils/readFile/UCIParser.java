package fca.utils.readFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static fca.utils.readFile.UCIContext.UciContext;

public class UCIParser {
    public static String UciParser(String name) throws IOException {
        String filename="src/main/java/data/uci/"+name;
        Path path = Paths.get(filename);
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<String> filterEmptyRows = lines.stream()
                .filter(line -> line.trim().length() > 0)
                .collect(Collectors.toList());
        int rowSize = filterEmptyRows.size();
        System.out.println("Number of non-empty rows: " + rowSize);
        HashMap<String, Integer> columnMap = new HashMap<>();
        for (String row : filterEmptyRows) {
            if (!row.trim().isEmpty()) {
                String[] columns = row.split(",");
                for (int i = 0; i < columns.length; i++) {
                    String key = genKey(i, columns[i]);
                    if (!columnMap.containsKey(key)) {
                        columnMap.put(key, columnMap.size());
                    }
                }
            }
        }
        int columnSize = columnMap.size();
        System.out.println("Number of columns: " + columnSize);
        boolean[][] data = new boolean[rowSize][columnSize];
        for (int rowIdx = 0; rowIdx < rowSize; rowIdx++) {
            String[] row = filterEmptyRows.get(rowIdx).split(",");
            for (int columnIdx = 0; columnIdx < row.length; columnIdx++) {
                String key = genKey(columnIdx, row[columnIdx]);
                Integer mappedColumnIdx = columnMap.get(key);
                if (mappedColumnIdx != null) {
                    data[rowIdx][mappedColumnIdx] = true;
                }
            }
        }
        return UciContext(rowSize, columnSize, data, name);

    }
        private static String genKey(int index, String value) {
            return index + ":" + value;
        }
}
