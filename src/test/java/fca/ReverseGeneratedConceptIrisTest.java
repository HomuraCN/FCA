package fca;

import fca.algorithm.reverse.ReverseGeneratedConcept;
import fca.utils.Context;
import fca.utils.concept.Concept;
import fca.utils.readFile.BitSetFileHandler;
import fca.utils.readFile.File;
import fca.utils.readFile.LatticeFileHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;

// 数据集测试
public class ReverseGeneratedConceptIrisTest {
    @Test
    public void testCombinationReduction() {
        try {
            ArrayList<Concept> concepts = LatticeFileHandler.readLatticeFromFile("src/main/java/data/lattice/iris_deduplication_lattice.data.txt");
            ArrayList<BitSet> reductions = BitSetFileHandler.readFromFile("src/main/java/data/reduction_permutations/iris_deduplication_reduction_permutations.data.txt");
            ReverseGeneratedConcept rc = new ReverseGeneratedConcept();
            ArrayList<Concept> combinationReduction = rc.combinationReduction(concepts, reductions);
            for (Concept concept : combinationReduction) {
                System.out.println(concept);
            }
            for (Concept concept : combinationReduction) {
                System.out.println(concept.getIntent());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void testReverseGeneratedContextTemp() {
        try {
            ArrayList<Concept> concepts = LatticeFileHandler.readLatticeFromFile("src/main/java/data/lattice/car_lattice.data.txt");
            ArrayList<BitSet> reductions = BitSetFileHandler.readFromFile("src/main/java/data/reduction/car_reduction.data.txt");
            ReverseGeneratedConcept rc = new ReverseGeneratedConcept();
            ArrayList<Concept> combinationReduction = rc.combinationReduction(concepts, reductions);

            Context context = File.readFile("src/main/java/data/context/car.data.txt");
            ReverseGeneratedConcept.reverseGeneratedContext_temp(context, combinationReduction);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void testReverseGeneratedContext() {
        try {
            ArrayList<Concept> concepts = LatticeFileHandler.readLatticeFromFile("src/main/java/data/lattice/iris_deduplication_lattice.data.txt");
            ArrayList<BitSet> reductions = BitSetFileHandler.readFromFile("src/main/java/data/reduction_permutations/iris_deduplication_reduction_permutations.data.txt");
            ReverseGeneratedConcept rc = new ReverseGeneratedConcept();
            ArrayList<Concept> combinationReduction = rc.combinationReduction(concepts, reductions);

            Context context = File.readFile("src/main/java/data/context/iris_deduplication.data.txt");
            ReverseGeneratedConcept.reverseGeneratedContext(context, combinationReduction, "iris");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void testReverseGeneratedConceptLatticeUsingExtents(){
        long startTime = System.currentTimeMillis();
        try {
            ArrayList<Concept> concepts = LatticeFileHandler.readLatticeFromFile("src/main/java/data/lattice/iris_deduplication_lattice.data.txt");
            Context reverseContext = File.readFile("src/main/java/data/reverse_context/iris_reverse.data.txt");
            ReverseGeneratedConcept.reverseGeneratedConceptLatticeUsingExtents(concepts, reverseContext, "iris");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("算法1,2共耗时: " + (endTime - startTime) + "ms");
    }
    @Test
    public void testReverseGeneratedConceptLatticeUsingIntents() {
        long startTime = System.currentTimeMillis();
        try {
            Context context = File.readFile("src/main/java/data/context/iris_deduplication.data.txt");
            ArrayList<Concept> concepts = LatticeFileHandler.readLatticeFromFile("src/main/java/data/lattice/iris_deduplication_lattice.data.txt");
            ReverseGeneratedConcept rc = new ReverseGeneratedConcept();
            ArrayList<BitSet> reductions = BitSetFileHandler.readFromFile("src/main/java/data/reduction_permutations/iris_deduplication_reduction_permutations.data.txt");
            ArrayList<Concept> combinationReduction = rc.combinationReduction(concepts, reductions);
            ReverseGeneratedConcept.reverseGeneratedConceptLatticeUsingIntents(context, concepts, combinationReduction, "iris");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("算法3共耗时: " + (endTime - startTime) + "ms");
    }
}
