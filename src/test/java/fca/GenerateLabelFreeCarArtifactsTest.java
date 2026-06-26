package fca;

import fca.algorithm.bipartite.ObjectConceptBipartiteExporter;
import fca.algorithm.concept.InClose3;
import fca.algorithm.concept3c_ae.ICbo53C_ae;
import fca.algorithm.concept3c_ae.PICbo53C_ae;
import fca.algorithm.encoding.ConceptPositionEncoder;
import fca.algorithm.graph.GraphBuilder;
import fca.utils.Context;
import fca.utils.concept.AEConcept;
import fca.utils.concept.AEConcept_id;
import fca.utils.concept.Concept;
import fca.utils.item.aeConceptItem;
import fca.utils.readFile.AELatticeFileHandler;
import fca.utils.readFile.File;
import fca.utils.readFile.LatticeFileHandler;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static fca.utils.util.get_objs_shared;
import static fca.utils.util.makeSet;

public class GenerateLabelFreeCarArtifactsTest {
    private static final String RAW_DATASET = "car";
    private static final String ARTIFACT_DATASET = "car_noleak";
    private static final int LABEL_COLUMN_INDEX = 6;
    private static final int PROFILE_BINS = ConceptPositionEncoder.DEFAULT_DEPTH_PROFILE_BINS;
    private static final int AE_SPLIT_DEPTH = 3;
    private static final int AE_THREADS = 16;

    @Test
    void generateLabelFreeCarArtifacts() throws IOException {
        writeLabelFreeContext();

        Context context = File.readFile(contextPath());
        ArrayList<Concept> positiveConcepts = buildPositiveLattice(context);
        ArrayList<AEConcept_id> negativeConcepts = buildCoreNegativeLattice(context);

        Files.createDirectories(Path.of("src/main/java/data/lattice"));
        Files.createDirectories(Path.of("src/main/java/data/lattice3C"));
        LatticeFileHandler.writeLatticeToFile(positiveConcepts, latticePath());
        AELatticeFileHandler.writeAELatticeToFile(negativeConcepts, aeLatticePath());

        Files.createDirectories(Path.of("src/main/java/data/graph"));
        GraphBuilder.writeMatrixToFile(
                GraphBuilder.buildAdjacencyMatrixUG(positiveConcepts, context.getObjs_size()),
                "src/main/java/data/graph/" + ARTIFACT_DATASET + "_A_plus_UG.csv");
        GraphBuilder.writeMatrixToFile(
                GraphBuilder.buildAEAdjacencyMatrixUG(negativeConcepts, context.getObjs_size()),
                "src/main/java/data/graph/" + ARTIFACT_DATASET + "_A_negative_UG.csv");

        writeDepthProfileCPE(context, positiveConcepts, negativeConcepts);
        writeObjectConceptBipartite(context, positiveConcepts, negativeConcepts);

        System.out.println("Generated label-free car FCA artifacts.");
        System.out.println("Context attributes: " + context.getAttrs_size());
        System.out.println("Positive concepts: " + positiveConcepts.size());
        System.out.println("Negative concepts: " + negativeConcepts.size());
    }

    private static void writeLabelFreeContext() throws IOException {
        Path inputPath = Path.of("src/main/java/data/uci/" + RAW_DATASET + ".data");
        Path outputPath = Path.of(contextPath());
        Files.createDirectories(outputPath.getParent());

        List<String> rows = Files.readAllLines(inputPath, StandardCharsets.UTF_8).stream()
                .filter(line -> !line.trim().isEmpty())
                .toList();

        Map<String, Integer> columnMap = new HashMap<>();
        List<String[]> parsedRows = new ArrayList<>();
        for (String row : rows) {
            String[] columns = row.split(",");
            parsedRows.add(columns);
            for (int i = 0; i < columns.length; i++) {
                if (i == LABEL_COLUMN_INDEX) {
                    continue;
                }
                String key = i + ":" + columns[i];
                columnMap.computeIfAbsent(key, ignored -> columnMap.size());
            }
        }

        boolean[][] context = new boolean[parsedRows.size()][columnMap.size()];
        for (int rowIndex = 0; rowIndex < parsedRows.size(); rowIndex++) {
            String[] columns = parsedRows.get(rowIndex);
            for (int columnIndex = 0; columnIndex < columns.length; columnIndex++) {
                if (columnIndex == LABEL_COLUMN_INDEX) {
                    continue;
                }
                Integer mappedColumn = columnMap.get(columnIndex + ":" + columns[columnIndex]);
                if (mappedColumn != null) {
                    context[rowIndex][mappedColumn] = true;
                }
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write(parsedRows.size() + "," + columnMap.size());
            writer.newLine();
            for (boolean[] row : context) {
                for (boolean value : row) {
                    writer.write(value ? "1," : "0,");
                }
                writer.newLine();
            }
        }
    }

    private static ArrayList<Concept> buildPositiveLattice(Context context) {
        Concept initialConcept = new Concept();
        initialConcept.setExtent(makeSet(context.getObjs_size()));
        initialConcept.setIntent(get_objs_shared(context, initialConcept.getExtent()));

        Queue<Concept> allConcepts = new LinkedList<>();
        InClose3.inClose3_exe(context, initialConcept, 1, new HashMap<>(), allConcepts);
        return new ArrayList<>(allConcepts);
    }

    private static ArrayList<AEConcept_id> buildCoreNegativeLattice(Context context) {
        AEConcept topConcept = new AEConcept();
        topConcept.setExtent(makeSet(0));
        topConcept.setExtent_n(makeSet(0));
        topConcept.setIntent(makeSet(context.getAttrs_size()));

        AEConcept startConcept = new AEConcept();
        startConcept.setExtent(makeSet(context.getObjs_size()));
        startConcept.setExtent_n(makeSet(context.getObjs_size()));
        startConcept.setIntent(makeSet(0));

        Queue<AEConcept> concepts = new LinkedList<>();
        Queue<aeConceptItem> splitQueue = new LinkedList<>();
        concepts.add(topConcept);
        PICbo53C_ae.PICbo53C_ae_exe(context, startConcept, 1, new BitSet(), AE_SPLIT_DEPTH, splitQueue, concepts);

        Map<Integer, Queue<aeConceptItem>> workQueues = new HashMap<>();
        Map<Integer, Queue<AEConcept>> partialResults = new HashMap<>();
        for (int i = 0; i < AE_THREADS; i++) {
            workQueues.put(i, new LinkedList<>());
            partialResults.put(i, new LinkedList<>());
        }

        int splitCount = splitQueue.size();
        for (int i = 0; i < splitCount; i++) {
            workQueues.get(i % AE_THREADS).offer(splitQueue.poll());
        }

        ExecutorService pool = Executors.newFixedThreadPool(AE_THREADS);
        ArrayList<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < AE_THREADS; i++) {
            Queue<aeConceptItem> workQueue = workQueues.get(i);
            Queue<AEConcept> resultQueue = partialResults.get(i);
            futures.add(pool.submit(() -> buildCoreNegativeLatticePartition(context, workQueue, resultQueue)));
        }
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException("Failed to build core AE lattice.", e);
            }
        }
        pool.shutdown();

        ArrayList<AEConcept_id> result = new ArrayList<>();
        int id = 1;
        for (AEConcept concept : concepts) {
            result.add(new AEConcept_id(id++, concept.getExtent(), concept.getExtent_n(), concept.getIntent()));
        }
        for (int i = 0; i < AE_THREADS; i++) {
            for (AEConcept concept : partialResults.get(i)) {
                result.add(new AEConcept_id(id++, concept.getExtent(), concept.getExtent_n(), concept.getIntent()));
            }
        }
        return result;
    }

    private static void buildCoreNegativeLatticePartition(Context context,
                                                         Queue<aeConceptItem> workQueue,
                                                         Queue<AEConcept> resultQueue) {
        if (workQueue.isEmpty()) {
            return;
        }
        for (aeConceptItem item : workQueue) {
            ICbo53C_ae.ICbo53C_ae_exe(
                    context,
                    item.getAeConcept(),
                    item.getJ(),
                    item.getP(),
                    resultQueue,
                    true);
        }
    }

    private static void writeDepthProfileCPE(Context context,
                                             ArrayList<Concept> positiveConcepts,
                                             ArrayList<AEConcept_id> negativeConcepts) throws IOException {
        Files.createDirectories(Path.of("src/main/java/data/cpe"));
        ConceptPositionEncoder.writeCPEToFile(
                ConceptPositionEncoder.computeObjectDepthProfileCPE(
                        positiveConcepts,
                        context.getObjs_size(),
                        PROFILE_BINS),
                "src/main/java/data/cpe/" + ARTIFACT_DATASET + "_CPE_A_plus_depth_profile" + PROFILE_BINS + ".csv");
        ConceptPositionEncoder.writeCPEToFile(
                ConceptPositionEncoder.computeNegativeObjectDepthProfileCPE(
                        negativeConcepts,
                        context.getObjs_size(),
                        PROFILE_BINS),
                "src/main/java/data/cpe/" + ARTIFACT_DATASET + "_CPE_A_negative_depth_profile" + PROFILE_BINS + ".csv");
    }

    private static void writeObjectConceptBipartite(Context context,
                                                    ArrayList<Concept> positiveConcepts,
                                                    ArrayList<AEConcept_id> negativeConcepts) throws IOException {
        Files.createDirectories(Path.of("src/main/java/data/bipartite"));
        ObjectConceptBipartiteExporter.exportPositive(
                positiveConcepts,
                context.getObjs_size(),
                context.getAttrs_size(),
                PROFILE_BINS,
                "src/main/java/data/bipartite/" + ARTIFACT_DATASET + "_positive_object_concept");
        ObjectConceptBipartiteExporter.exportNegative(
                negativeConcepts,
                context.getObjs_size(),
                context.getAttrs_size(),
                PROFILE_BINS,
                "src/main/java/data/bipartite/" + ARTIFACT_DATASET + "_negative_object_concept");
    }

    private static String contextPath() {
        return "src/main/java/data/context/" + ARTIFACT_DATASET + ".data.txt";
    }

    private static String latticePath() {
        return "src/main/java/data/lattice/" + ARTIFACT_DATASET + "_lattice.data.txt";
    }

    private static String aeLatticePath() {
        return "src/main/java/data/lattice3C/" + ARTIFACT_DATASET + "_aelattice.data.txt";
    }
}
