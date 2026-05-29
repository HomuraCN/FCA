package fca;

import fca.algorithm.encoding.ConceptPositionEncoder;
import fca.algorithm.encoding.ConceptPositionEncoder.DepthAggregationMode;
import fca.utils.Context;
import fca.utils.concept.AEConcept_id;
import fca.utils.concept.Concept;
import fca.utils.readFile.AELatticeFileHandler;
import fca.utils.readFile.File;
import fca.utils.readFile.LatticeFileHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ConceptPositionEncoderGenerationTest {

    @Test
    void testBuildAndWriteCPE() {
        try {
            String datasetName = "car"; // 需要切换数据集时，修改这里即可，例如 zoo 或 car

            Context context = File.readFile("src/main/java/data/context/" + datasetName + ".data.txt");
            int objectCount = context.getObjs_size();

            String outputDir = "src/main/java/data/cpe/";
            Files.createDirectories(Paths.get(outputDir));

            ArrayList<Concept> positiveConcepts = LatticeFileHandler.readLatticeFromFile(
                    "src/main/java/data/lattice/" + datasetName + "_lattice.data.txt");
            ArrayList<AEConcept_id> negativeConcepts = AELatticeFileHandler.readAELatticeFromFile(
                    "src/main/java/data/lattice3C/" + datasetName + "_aelattice.data.txt");

            writePositiveCPE(datasetName, outputDir, positiveConcepts, objectCount, DepthAggregationMode.MAX);
            writeNegativeCPE(datasetName, outputDir, negativeConcepts, objectCount, DepthAggregationMode.MAX);
            writePositiveCPE(datasetName, outputDir, positiveConcepts, objectCount, DepthAggregationMode.SUPPORT_WEIGHTED_MEAN);
            writeNegativeCPE(datasetName, outputDir, negativeConcepts, objectCount, DepthAggregationMode.SUPPORT_WEIGHTED_MEAN);
            writePositiveDepthProfileCPE(
                    datasetName,
                    outputDir,
                    positiveConcepts,
                    objectCount,
                    ConceptPositionEncoder.DEFAULT_DEPTH_PROFILE_BINS);
            writeNegativeDepthProfileCPE(
                    datasetName,
                    outputDir,
                    negativeConcepts,
                    objectCount,
                    ConceptPositionEncoder.DEFAULT_DEPTH_PROFILE_BINS);

            System.out.println(datasetName + " 数据集 CPE 生成完成。");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writePositiveCPE(String datasetName,
                                         String outputDir,
                                         ArrayList<Concept> concepts,
                                         int objectCount,
                                         DepthAggregationMode depthMode) throws IOException {
        double[][] cpe = ConceptPositionEncoder.computeObjectCPE(concepts, objectCount, depthMode);
        ConceptPositionEncoder.writeCPEToFile(
                cpe,
                outputDir + datasetName + "_CPE_A_plus" + depthModeSuffix(depthMode) + ".csv");
    }

    private static void writeNegativeCPE(String datasetName,
                                         String outputDir,
                                         ArrayList<AEConcept_id> concepts,
                                         int objectCount,
                                         DepthAggregationMode depthMode) throws IOException {
        double[][] cpe = ConceptPositionEncoder.computeNegativeObjectCPE(concepts, objectCount, depthMode);
        ConceptPositionEncoder.writeCPEToFile(
                cpe,
                outputDir + datasetName + "_CPE_A_negative" + depthModeSuffix(depthMode) + ".csv");
    }

    private static String depthModeSuffix(DepthAggregationMode depthMode) {
        return switch (depthMode) {
            case MAX -> "_max_depth";
            case SUPPORT_WEIGHTED_MEAN -> "_weighted_depth";
            case MEAN -> "";
        };
    }

    private static void writePositiveDepthProfileCPE(String datasetName,
                                                     String outputDir,
                                                     ArrayList<Concept> concepts,
                                                     int objectCount,
                                                     int binCount) throws IOException {
        double[][] cpe = ConceptPositionEncoder.computeObjectDepthProfileCPE(concepts, objectCount, binCount);
        ConceptPositionEncoder.writeCPEToFile(
                cpe,
                outputDir + datasetName + "_CPE_A_plus_depth_profile" + binCount + ".csv");
    }

    private static void writeNegativeDepthProfileCPE(String datasetName,
                                                     String outputDir,
                                                     ArrayList<AEConcept_id> concepts,
                                                     int objectCount,
                                                     int binCount) throws IOException {
        double[][] cpe = ConceptPositionEncoder.computeNegativeObjectDepthProfileCPE(concepts, objectCount, binCount);
        ConceptPositionEncoder.writeCPEToFile(
                cpe,
                outputDir + datasetName + "_CPE_A_negative_depth_profile" + binCount + ".csv");
    }
}
