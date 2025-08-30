package fca;

import fca.algorithm.reduction.permutations;
import fca.utils.readFile.BitSetFileHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;

public class PermutationsTest {
    @Test
    public void testPermutations() {
        try {
            ArrayList<BitSet> bitSetArrayList = BitSetFileHandler.readFromFile("src/main/java/data/reduction/iris_deduplication_reduction.data.txt");
            ArrayList<BitSet> reductions = permutations.permutations_exe(bitSetArrayList);
            System.out.println(reductions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
