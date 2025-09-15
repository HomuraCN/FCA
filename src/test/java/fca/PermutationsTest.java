package fca;

import fca.algorithm.reduction.permutations;
import fca.utils.concept.Concept;
import fca.utils.readFile.BitSetFileHandler;
import fca.utils.readFile.LatticeFileHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;

public class PermutationsTest {
    @Test
    public void testPermutations() {
        try {
            ArrayList<BitSet> bitSetArrayList = BitSetFileHandler.readFromFile("src/main/java/data/reduction/test_deduplication_reduction.data.txt");
            ArrayList<BitSet> reductions = permutations.permutations_exe(bitSetArrayList);
            System.out.println(reductions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void testPermutations2() {
        try {
            ArrayList<BitSet> reductions_permutations = BitSetFileHandler.readFromFile("src/main/java/data/reduction_permutations/test_deduplication_reduction_permutations.data.txt");
            ArrayList<Concept>  reverseConcepts = LatticeFileHandler.readLatticeFromFile("src/main/java/data/lattice/test_deduplication_lattice.data.txt");
            System.out.println(reductions_permutations);
            System.out.println(reverseConcepts);
            BitSet rp = reductions_permutations.get(0);
            int cnt = 0;
            ArrayList<BitSet> B_i = new ArrayList<>();
            for(int i = rp.nextSetBit(0); i >= 0; i = rp.nextSetBit(i+1)) {
                if (i < reverseConcepts.size() + 1) {
                    Concept concept = reverseConcepts.get(i - 1);
                    System.out.println("B_" + cnt++ + ":" + concept.getIntent());
                    B_i.add(concept.getIntent());
                }
            }
            for (BitSet b : B_i) {
                System.out.println(b);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
