package fca;

import fca.algorithm.reverse.ReverseGeneratedConcept;
import fca.analysis.Analysis;
import fca.utils.Context;
import fca.utils.concept.Concept;
import fca.utils.readFile.BitSetFileHandler;
import fca.utils.readFile.File;
import fca.utils.readFile.LatticeFileHandler;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;

public class AnalysisTest {
    @Test
    void IRDTest() {
        try {
            ArrayList<BitSet> reductions_permutations = BitSetFileHandler.readFromFile("src/main/java/data/reduction_permutations/test_deduplication_reduction_permutations.data.txt");
            ArrayList<Concept> originalLattice = LatticeFileHandler.readLatticeFromFile("src/main/java/data/lattice/test_deduplication_lattice.data.txt");
            BitSet reductionPermutation = reductions_permutations.getFirst();

            Context originalContext = File.readFile("src/main/java/data/context/test_deduplication.data.txt");
            Context reverseContext = File.readFile("src/main/java/data/reverse_context/test_reverse.data.txt");

            double ird = Analysis.calculateIRD(
                    originalContext,
                    reverseContext,
                    reductionPermutation, // 注意这里传入的是单个BitSet
                    originalLattice
            );

            System.out.println("信息还原度 (IRD): " + ird);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void NRRTest() {
        try {
            ArrayList<Concept> originalLattice = LatticeFileHandler.readLatticeFromFile("src/main/java/data/lattice/test_deduplication_lattice.data.txt");
            ArrayList<Concept> reverseLattice = LatticeFileHandler.readLatticeFromFile("src/main/java/data/reverse_lattice/test_lattice_reverse_extents.data.txt");

            double nrr = Analysis.calculateNodeReductionRate(
                    originalLattice,
                    reverseLattice
            );

            System.out.println("节点减少率 (NRR): " + nrr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void AllTest(){
        try {
            ArrayList<BitSet> reductions_permutations = BitSetFileHandler.readFromFile("src/main/java/data/reduction_permutations/iris_deduplication_reduction_permutations.data.txt");
            ArrayList<Concept> originalLattice = LatticeFileHandler.readLatticeFromFile("src/main/java/data/lattice/iris_deduplication_lattice.data.txt");
            BitSet reductionPermutation = reductions_permutations.getFirst();

            Context originalContext = File.readFile("src/main/java/data/context/iris_deduplication.data.txt");
            Context reverseContext = File.readFile("src/main/java/data/reverse_context/iris_reverse.data.txt");

            ArrayList<Concept> reverseLattice = LatticeFileHandler.readLatticeFromFile("src/main/java/data/reverse_lattice/iris_lattice_reverse_extents.data.txt");

            double ird = Analysis.calculateIRD(
                    originalContext,
                    reverseContext,
                    reductionPermutation, // 注意这里传入的是单个BitSet
                    originalLattice
            );

            double nrr = Analysis.calculateNodeReductionRate(
                    originalLattice,
                    reverseLattice
            );

            System.out.println("信息还原度 (IRD): " + ird);
            System.out.println("节点减少率 (NRR): " + nrr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void AlgorithmAnalysisTest(){
        String fileName = "test";
        try {
            Context originalContext = File.readFile("src/main/java/data/context/" + fileName + "_deduplication.data.txt");
            ArrayList<Concept> originalLattice = LatticeFileHandler.readLatticeFromFile("src/main/java/data/lattice/" + fileName + "_deduplication_lattice.data.txt");

            ReverseGeneratedConcept rc = new  ReverseGeneratedConcept();
            ArrayList<BitSet> reductions = BitSetFileHandler.readFromFile("src/main/java/data/reduction_permutations/" + fileName + "_deduplication_reduction_permutations.data.txt");
            ArrayList<ArrayList<Concept>> allCombinationReduction = rc.getAllCombinationReduction(originalLattice, reductions);

            for (ArrayList<Concept> combination : allCombinationReduction) {
                long start = System.currentTimeMillis();

                ReverseGeneratedConcept.reverseGeneratedContext(originalContext, combination, fileName);
                Context reverseContext = File.readFile("src/main/java/data/reverse_context/" + fileName + "_reverse.data.txt");
                ReverseGeneratedConcept.reverseGeneratedConceptLatticeUsingExtents(originalLattice, reverseContext, fileName);
                ArrayList<Concept> reverseLattice = LatticeFileHandler.readLatticeFromFile("src/main/java/data/reverse_lattice/" + fileName + "_lattice_reverse_extents.data.txt");

                long end = System.currentTimeMillis();

                BitSet reductionPermutation = new BitSet();
                for (Concept c : combination) {
                    reductionPermutation.set(c.getId());
                }

                double ird = Analysis.calculateIRD(
                        originalContext,
                        reverseContext,
                        reductionPermutation, // 注意这里传入的是单个BitSet
                        originalLattice
                );

                double nrr = Analysis.calculateNodeReductionRate(
                        originalLattice,
                        reverseLattice
                );

                System.out.println("信息还原度 (IRD): " + ird);
                System.out.println("节点减少率 (NRR): " + nrr);
                System.out.println("构建时间 (构建背景&构建概念格): " + (end - start) + "ms");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void AlgorithmAnalysisCsvTest(){
        String fileName = "Maternal Health Risk Data Set";
        // 定义输出CSV文件的路径
        String outputCsvFile = "src/main/java/data/analysis/" + fileName + "_analysis_results.csv";

        // 使用 try-with-resources 确保 FileWriter 和 PrintWriter 被自动关闭
        try (FileWriter fw = new FileWriter(outputCsvFile);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)){

            // 写入CSV文件的表头
            out.println("CombinationReductionName,IRD,NRR,BuildTime");
            int reductionCounter = 1;

            Context originalContext = File.readFile("src/main/java/data/context/" + fileName + "_deduplication.data.txt");
            ArrayList<Concept> originalLattice = LatticeFileHandler.readLatticeFromFile("src/main/java/data/lattice/" + fileName + "_deduplication_lattice.data.txt");

            ReverseGeneratedConcept rc = new  ReverseGeneratedConcept();
            ArrayList<BitSet> reductions = BitSetFileHandler.readFromFile("src/main/java/data/reduction_permutations/" + fileName + "_deduplication_reduction_permutations.data.txt");
            ArrayList<ArrayList<Concept>> allCombinationReduction = rc.getAllCombinationReduction(originalLattice, reductions);

            for (ArrayList<Concept> combination : allCombinationReduction) {
                long start = System.currentTimeMillis();

                ReverseGeneratedConcept.reverseGeneratedContext(originalContext, combination, fileName);
                Context reverseContext = File.readFile("src/main/java/data/reverse_context/" + fileName + "_reverse.data.txt");
                ReverseGeneratedConcept.reverseGeneratedConceptLatticeUsingExtents(originalLattice, reverseContext, fileName);
                ArrayList<Concept> reverseLattice = LatticeFileHandler.readLatticeFromFile("src/main/java/data/reverse_lattice/" + fileName + "_lattice_reverse_extents.data.txt");

                long end = System.currentTimeMillis();

                BitSet reductionPermutation = new BitSet();
                for (Concept c : combination) {
                    reductionPermutation.set(c.getId());
                }

                double ird = Analysis.calculateIRD(
                        originalContext,
                        reverseContext,
                        reductionPermutation, // 注意这里传入的是单个BitSet
                        originalLattice
                );

                double nrr = Analysis.calculateNodeReductionRate(
                        originalLattice,
                        reverseLattice
                );

                // 为当前约简方案命名
                String reductionName = "CombinationReduction_" + reductionCounter++;

                // 将结果格式化为CSV的一行并写入文件
                out.println(reductionName + "," + ird + "," + nrr + "," + (end - start));


                System.out.println("信息还原度 (IRD): " + ird);
                System.out.println("节点减少率 (NRR): " + nrr);
                System.out.println("构建时间 (构建背景&构建概念格): " + (end - start) + "ms");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
