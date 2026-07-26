package fca.algorithm.bipartite;

import fca.algorithm.encoding.ConceptPositionEncoder;
import fca.utils.concept.AEConcept_id;
import fca.utils.concept.Concept;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;

/**
 * 导出对象-概念二部图所需的稀疏边表和概念节点特征。
 *
 * <p>边表不再生成对象-对象的 N x N 稠密邻接矩阵，而是直接表示
 * “对象属于某个概念”的 membership 关系：</p>
 *
 * <pre>
 * object_id,concept_id,weight
 * </pre>
 *
 * <p>object_id 与 concept_id 均从 0 开始，方便 Python/PyG 直接读取。</p>
 */
public class ObjectConceptBipartiteExporter {

    private ObjectConceptBipartiteExporter() {
    }

    public static void exportPositive(ArrayList<Concept> concepts,
                                      int objectCount,
                                      int attributeCount,
                                      int profileBins,
                                      String outputPrefix) throws IOException {
        int[] depths = ConceptPositionEncoder.computeConceptDepths(concepts);
        exportPositive(concepts, objectCount, attributeCount, profileBins, outputPrefix, depths);
    }

    /**
     * 使用外部预计算的概念分级导出，避免重复的深度/分级计算。
     */
    public static void exportPositive(ArrayList<Concept> concepts,
                                      int objectCount,
                                      int attributeCount,
                                      int profileBins,
                                      String outputPrefix,
                                      int[] depths) throws IOException {
        writePositiveMembershipEdges(concepts, objectCount, outputPrefix + "_edges.csv");
        writePositiveConceptFeatures(
                concepts,
                depths,
                objectCount,
                attributeCount,
                profileBins,
                outputPrefix + "_concept_features.csv");
    }

    public static void exportNegative(ArrayList<AEConcept_id> concepts,
                                      int objectCount,
                                      int attributeCount,
                                      int profileBins,
                                      String outputPrefix) throws IOException {
        int[] depths = ConceptPositionEncoder.computeNegativeConceptDepths(concepts);
        exportNegative(concepts, objectCount, attributeCount, profileBins, outputPrefix, depths);
    }

    /**
     * 使用外部预计算的概念分级导出，避免重复的深度/分级计算。
     */
    public static void exportNegative(ArrayList<AEConcept_id> concepts,
                                      int objectCount,
                                      int attributeCount,
                                      int profileBins,
                                      String outputPrefix,
                                      int[] depths) throws IOException {
        writeNegativeMembershipEdges(concepts, objectCount, outputPrefix + "_edges.csv");
        writeNegativeConceptFeatures(
                concepts,
                depths,
                objectCount,
                attributeCount,
                profileBins,
                outputPrefix + "_concept_features.csv");
    }

    private static void writePositiveMembershipEdges(ArrayList<Concept> concepts,
                                                     int objectCount,
                                                     String outputPath) throws IOException {
        ensureParentDirectory(outputPath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("object_id,concept_id,weight");
            writer.newLine();
            for (int conceptIndex = 0; conceptIndex < concepts.size(); conceptIndex++) {
                BitSet extent = safeExtent(concepts.get(conceptIndex).getExtent());
                writeMembershipEdges(writer, extent, objectCount, conceptIndex);
            }
        }
        System.out.println("正概念二部图边表已写入: " + outputPath);
    }

    private static void writeNegativeMembershipEdges(ArrayList<AEConcept_id> concepts,
                                                     int objectCount,
                                                     String outputPath) throws IOException {
        ensureParentDirectory(outputPath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("object_id,concept_id,weight");
            writer.newLine();
            for (int conceptIndex = 0; conceptIndex < concepts.size(); conceptIndex++) {
                BitSet extent = safeExtent(concepts.get(conceptIndex).getExtent_n());
                writeMembershipEdges(writer, extent, objectCount, conceptIndex);
            }
        }
        System.out.println("负概念二部图边表已写入: " + outputPath);
    }

    private static void writeMembershipEdges(BufferedWriter writer,
                                             BitSet extent,
                                             int objectCount,
                                             int conceptIndex) throws IOException {
        int support = extent.cardinality();
        double weight = support > 0 ? 1.0 / Math.log1p(support) : 0.0;
        for (int objectId = extent.nextSetBit(1);
             objectId >= 0 && objectId <= objectCount;
             objectId = extent.nextSetBit(objectId + 1)) {
            writer.write(Integer.toString(objectId - 1));
            writer.write(",");
            writer.write(Integer.toString(conceptIndex));
            writer.write(",");
            writer.write(Double.toString(weight));
            writer.newLine();
        }
    }

    private static void writePositiveConceptFeatures(ArrayList<Concept> concepts,
                                                     int[] depths,
                                                     int objectCount,
                                                     int attributeCount,
                                                     int profileBins,
                                                     String outputPath) throws IOException {
        ensureParentDirectory(outputPath);
        int maxDepth = max(depths);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            for (int conceptIndex = 0; conceptIndex < concepts.size(); conceptIndex++) {
                Concept concept = concepts.get(conceptIndex);
                writeConceptFeatureRow(
                        writer,
                        safeExtent(concept.getExtent()),
                        safeExtent(concept.getIntent()),
                        depths[conceptIndex],
                        maxDepth,
                        objectCount,
                        attributeCount,
                        profileBins);
            }
        }
        System.out.println("正概念节点特征已写入: " + outputPath);
    }

    private static void writeNegativeConceptFeatures(ArrayList<AEConcept_id> concepts,
                                                     int[] depths,
                                                     int objectCount,
                                                     int attributeCount,
                                                     int profileBins,
                                                     String outputPath) throws IOException {
        ensureParentDirectory(outputPath);
        int maxDepth = max(depths);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            for (int conceptIndex = 0; conceptIndex < concepts.size(); conceptIndex++) {
                AEConcept_id concept = concepts.get(conceptIndex);
                writeConceptFeatureRow(
                        writer,
                        safeExtent(concept.getExtent_n()),
                        safeExtent(concept.getIntent()),
                        depths[conceptIndex],
                        maxDepth,
                        objectCount,
                        attributeCount,
                        profileBins);
            }
        }
        System.out.println("负概念节点特征已写入: " + outputPath);
    }

    private static void writeConceptFeatureRow(BufferedWriter writer,
                                               BitSet extent,
                                               BitSet intent,
                                               int depth,
                                               int maxDepth,
                                               int objectCount,
                                               int attributeCount,
                                               int profileBins) throws IOException {
        int bins = Math.max(profileBins, 1);
        int binIndex = getDepthBin(depth, maxDepth, bins);

        for (int i = 0; i < bins; i++) {
            writer.write(i == binIndex ? "1.0" : "0.0");
            writer.write(",");
        }

        double normalizedDepth = maxDepth > 0 ? (double) depth / maxDepth : 0.0;
        double normalizedSupport = objectCount > 0 ? (double) extent.cardinality() / objectCount : 0.0;
        double normalizedIntentSize = attributeCount > 0 ? (double) intent.cardinality() / attributeCount : 0.0;
        double specificity = extent.cardinality() > 0 ? 1.0 / Math.log1p(extent.cardinality()) : 0.0;

        writer.write(Double.toString(normalizedDepth));
        writer.write(",");
        writer.write(Double.toString(normalizedSupport));
        writer.write(",");
        writer.write(Double.toString(normalizedIntentSize));
        writer.write(",");
        writer.write(Double.toString(specificity));
        writer.newLine();
    }

    private static int getDepthBin(int depth, int maxDepth, int bins) {
        if (maxDepth <= 0) {
            return 0;
        }
        int binIndex = (int) Math.floor((double) depth * bins / (maxDepth + 1));
        return Math.min(binIndex, bins - 1);
    }

    private static int max(int[] values) {
        int max = 0;
        for (int value : values) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    private static BitSet safeExtent(BitSet extent) {
        return extent == null ? new BitSet() : extent;
    }

    private static void ensureParentDirectory(String outputPath) throws IOException {
        Path parent = Path.of(outputPath).getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}
