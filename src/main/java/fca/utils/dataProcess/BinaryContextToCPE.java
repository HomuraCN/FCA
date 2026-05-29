package fca.utils.dataProcess;

import fca.algorithm.encoding.ConceptPositionEncoder;
import fca.algorithm.encoding.ConceptPositionEncoder.DepthAggregationMode;
import fca.utils.Context;
import fca.utils.concept.AEConcept_id;
import fca.utils.concept.Concept;
import fca.utils.readFile.AELatticeFileHandler;
import fca.utils.readFile.File;
import fca.utils.readFile.LatticeFileHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * 根据已经生成好的概念格文件，导出对象级概念位置编码（CPE）CSV 文件。
 *
 * <p>命令行用法：
 * java ... fca.utils.dataProcess.BinaryContextToCPE iris positive
 * java ... fca.utils.dataProcess.BinaryContextToCPE iris negative
 * java ... fca.utils.dataProcess.BinaryContextToCPE iris both max
 * java ... fca.utils.dataProcess.BinaryContextToCPE iris both weighted
 * java ... fca.utils.dataProcess.BinaryContextToCPE iris both profile 8</p>
 */
public class BinaryContextToCPE {

    public static void main(String[] args) {
        String datasetName = args.length >= 1 ? args[0] : "iris";
        String view = args.length >= 2 ? args[1] : "positive";
        boolean useDepthProfile = args.length >= 3 && "profile".equalsIgnoreCase(args[2]);
        DepthAggregationMode depthMode = args.length >= 3
                ? parseDepthMode(args[2])
                : DepthAggregationMode.MEAN;
        int profileBins = args.length >= 4
                ? Integer.parseInt(args[3])
                : ConceptPositionEncoder.DEFAULT_DEPTH_PROFILE_BINS;

        try {
            String contextPath = "src/main/java/data/context/" + datasetName + ".data.txt";
            Context context = File.readFile(contextPath);
            int objectCount = context.getObjs_size();

            String outputDir = "src/main/java/data/cpe/";
            Files.createDirectories(Paths.get(outputDir));

            if ("negative".equalsIgnoreCase(view) || "neg".equalsIgnoreCase(view)) {
                if (useDepthProfile) {
                    generateNegativeDepthProfileCPE(datasetName, objectCount, outputDir, profileBins);
                } else {
                    generateNegativeCPE(datasetName, objectCount, outputDir, depthMode);
                }
            } else if ("positive".equalsIgnoreCase(view) || "pos".equalsIgnoreCase(view)) {
                if (useDepthProfile) {
                    generatePositiveDepthProfileCPE(datasetName, objectCount, outputDir, profileBins);
                } else {
                    generatePositiveCPE(datasetName, objectCount, outputDir, depthMode);
                }
            } else if ("both".equalsIgnoreCase(view)) {
                if (useDepthProfile) {
                    generatePositiveDepthProfileCPE(datasetName, objectCount, outputDir, profileBins);
                    generateNegativeDepthProfileCPE(datasetName, objectCount, outputDir, profileBins);
                } else {
                    generatePositiveCPE(datasetName, objectCount, outputDir, depthMode);
                    generateNegativeCPE(datasetName, objectCount, outputDir, depthMode);
                }
            } else {
                throw new IllegalArgumentException("Unknown view: " + view + ". Use positive, negative, or both.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate CPE.", e);
        }
    }

    private static void generatePositiveCPE(String datasetName,
                                            int objectCount,
                                            String outputDir,
                                            DepthAggregationMode depthMode) throws IOException {
        String latticePath = "src/main/java/data/lattice/" + datasetName + "_lattice.data.txt";
        ArrayList<Concept> concepts = LatticeFileHandler.readLatticeFromFile(latticePath);
        double[][] cpe = ConceptPositionEncoder.computeObjectCPE(concepts, objectCount, depthMode);
        String outputPath = outputDir + datasetName + "_CPE_A_plus" + depthModeSuffix(depthMode) + ".csv";
        ConceptPositionEncoder.writeCPEToFile(cpe, outputPath);
    }

    private static void generateNegativeCPE(String datasetName,
                                            int objectCount,
                                            String outputDir,
                                            DepthAggregationMode depthMode) throws IOException {
        String latticePath = "src/main/java/data/lattice3C/" + datasetName + "_aelattice.data.txt";
        ArrayList<AEConcept_id> concepts = AELatticeFileHandler.readAELatticeFromFile(latticePath);
        double[][] cpe = ConceptPositionEncoder.computeNegativeObjectCPE(concepts, objectCount, depthMode);
        String outputPath = outputDir + datasetName + "_CPE_A_negative" + depthModeSuffix(depthMode) + ".csv";
        ConceptPositionEncoder.writeCPEToFile(cpe, outputPath);
    }

    private static DepthAggregationMode parseDepthMode(String value) {
        if ("profile".equalsIgnoreCase(value)) {
            return DepthAggregationMode.MEAN;
        }
        if ("max".equalsIgnoreCase(value)) {
            return DepthAggregationMode.MAX;
        }
        if ("weighted".equalsIgnoreCase(value) || "support_weighted_mean".equalsIgnoreCase(value)) {
            return DepthAggregationMode.SUPPORT_WEIGHTED_MEAN;
        }
        if ("mean".equalsIgnoreCase(value)) {
            return DepthAggregationMode.MEAN;
        }
        throw new IllegalArgumentException("Unknown depth mode: " + value + ". Use mean, max, or weighted.");
    }

    private static String depthModeSuffix(DepthAggregationMode depthMode) {
        return switch (depthMode) {
            case MAX -> "_max_depth";
            case SUPPORT_WEIGHTED_MEAN -> "_weighted_depth";
            case MEAN -> "";
        };
    }

    private static void generatePositiveDepthProfileCPE(String datasetName,
                                                        int objectCount,
                                                        String outputDir,
                                                        int binCount) throws IOException {
        String latticePath = "src/main/java/data/lattice/" + datasetName + "_lattice.data.txt";
        ArrayList<Concept> concepts = LatticeFileHandler.readLatticeFromFile(latticePath);
        double[][] cpe = ConceptPositionEncoder.computeObjectDepthProfileCPE(concepts, objectCount, binCount);
        String outputPath = outputDir + datasetName + "_CPE_A_plus_depth_profile" + binCount + ".csv";
        ConceptPositionEncoder.writeCPEToFile(cpe, outputPath);
    }

    private static void generateNegativeDepthProfileCPE(String datasetName,
                                                        int objectCount,
                                                        String outputDir,
                                                        int binCount) throws IOException {
        String latticePath = "src/main/java/data/lattice3C/" + datasetName + "_aelattice.data.txt";
        ArrayList<AEConcept_id> concepts = AELatticeFileHandler.readAELatticeFromFile(latticePath);
        double[][] cpe = ConceptPositionEncoder.computeNegativeObjectDepthProfileCPE(concepts, objectCount, binCount);
        String outputPath = outputDir + datasetName + "_CPE_A_negative_depth_profile" + binCount + ".csv";
        ConceptPositionEncoder.writeCPEToFile(cpe, outputPath);
    }
}
