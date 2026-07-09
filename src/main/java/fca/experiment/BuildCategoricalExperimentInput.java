package fca.experiment;

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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.GZIPOutputStream;

import static fca.utils.util.get_objs_shared;
import static fca.utils.util.makeSet;

public class BuildCategoricalExperimentInput {
    private static final int DEFAULT_PROFILE_BINS = ConceptPositionEncoder.DEFAULT_DEPTH_PROFILE_BINS;
    private static final int DEFAULT_AE_SPLIT_DEPTH = 3;
    private static final int DEFAULT_AE_THREADS = 16;

    public static void main(String[] args) throws Exception {
        Config config = Config.parse(args);
        build(config);
    }

    private static void build(Config config) throws Exception {
        Path outputDir = config.outputRoot.resolve(config.dataset + "_input").toAbsolutePath().normalize();
        Files.createDirectories(outputDir);

        Path dataPath = config.inputDir.resolve(config.dataset + ".csv");
        Path columnPath = config.inputDir.resolve(config.dataset + "_col.csv");
        List<String> columnNames = readColumnNames(columnPath);
        List<String[]> rows = readRows(dataPath, columnNames.size());
        int labelColumnIndex = resolveLabelColumn(config.labelColumn, columnNames);

        EncodedDataset encoded = encodeCategoricalRows(rows, columnNames, labelColumnIndex);
        writeRawCopy(dataPath, columnPath, outputDir, config.dataset);
        writeFeatureMatrix(outputDir.resolve(config.dataset + ".data.cleaned.csv"), encoded.featureMatrix);
        writeLabels(outputDir.resolve(config.dataset + ".labels.csv"), encoded.labels);
        writeLabelMapping(outputDir.resolve(config.dataset + ".label_mapping.csv"), encoded.labels);
        writeFeatureColumns(outputDir.resolve(config.dataset + ".feature_columns.csv"), encoded.featureNames);
        writeContext(outputDir.resolve(config.dataset + ".data.txt"), encoded.featureMatrix);

        Context context = File.readFile(outputDir.resolve(config.dataset + ".data.txt").toString());
        ArrayList<Concept> positiveConcepts = buildPositiveLattice(context);
        ArrayList<AEConcept_id> negativeConcepts = buildCoreNegativeLattice(
                context,
                config.aeSplitDepth,
                config.aeThreads);

        LatticeFileHandler.writeLatticeToFile(
                positiveConcepts,
                outputDir.resolve(config.dataset + "_lattice.data.txt").toString());
        AELatticeFileHandler.writeAELatticeToFile(
                negativeConcepts,
                outputDir.resolve(config.dataset + "_aelattice.data.txt").toString());

        if (config.skipAdjacency) {
            Files.deleteIfExists(outputDir.resolve(config.dataset + "_A_plus_UG.csv"));
            Files.deleteIfExists(outputDir.resolve(config.dataset + "_A_negative_UG.csv"));
            System.out.println("Skipped positive/negative adjacency matrix generation.");
        } else {
            GraphBuilder.writeMatrixToFile(
                    GraphBuilder.buildAdjacencyMatrixUG(positiveConcepts, context.getObjs_size()),
                    outputDir.resolve(config.dataset + "_A_plus_UG.csv").toString());
            GraphBuilder.writeMatrixToFile(
                    GraphBuilder.buildAEAdjacencyMatrixUG(negativeConcepts, context.getObjs_size()),
                    outputDir.resolve(config.dataset + "_A_negative_UG.csv").toString());
        }

        ConceptPositionEncoder.writeCPEToFile(
                ConceptPositionEncoder.computeObjectDepthProfileCPE(
                        positiveConcepts,
                        context.getObjs_size(),
                        config.profileBins),
                outputDir.resolve(config.dataset + "_CPE_A_plus_depth_profile" + config.profileBins + ".csv").toString());
        ConceptPositionEncoder.writeCPEToFile(
                ConceptPositionEncoder.computeNegativeObjectDepthProfileCPE(
                        negativeConcepts,
                        context.getObjs_size(),
                        config.profileBins),
                outputDir.resolve(config.dataset + "_CPE_A_negative_depth_profile" + config.profileBins + ".csv").toString());

        writeObjectConceptBipartite(outputDir, config.dataset, context, positiveConcepts, negativeConcepts, config.profileBins);
        writeManifest(outputDir.resolve("manifest.json"), config, outputDir, columnNames, labelColumnIndex, encoded, positiveConcepts, negativeConcepts);

        System.out.println();
        System.out.println("Generated experiment input directory: " + outputDir);
        System.out.println("Rows: " + encoded.featureMatrix.size());
        System.out.println("Feature columns: " + encoded.featureNames.size());
        System.out.println("Positive concepts: " + positiveConcepts.size());
        System.out.println("Negative concepts: " + negativeConcepts.size());
    }

    private static List<String> readColumnNames(Path columnPath) throws IOException {
        List<String> lines = Files.readAllLines(columnPath, StandardCharsets.UTF_8).stream()
                .filter(line -> !line.isBlank())
                .toList();
        if (lines.size() != 1) {
            throw new IllegalArgumentException("Column file must contain exactly one non-empty line: " + columnPath);
        }
        return parseCsvLine(lines.get(0));
    }

    private static List<String[]> readRows(Path dataPath, int expectedColumns) throws IOException {
        List<String[]> rows = new ArrayList<>();
        int lineNo = 0;
        for (String line : Files.readAllLines(dataPath, StandardCharsets.UTF_8)) {
            lineNo++;
            if (line.isBlank()) {
                continue;
            }
            List<String> columns = parseCsvLine(line);
            if (columns.size() != expectedColumns) {
                throw new IllegalArgumentException(
                        "Column count mismatch at " + dataPath + ":" + lineNo
                                + ", expected " + expectedColumns + ", got " + columns.size());
            }
            rows.add(columns.toArray(String[]::new));
        }
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Data file has no rows: " + dataPath);
        }
        return rows;
    }

    private static List<String> parseCsvLine(String line) {
        String[] parts = line.split(",", -1);
        List<String> values = new ArrayList<>(parts.length);
        for (String part : parts) {
            if (part.isEmpty()) {
                throw new IllegalArgumentException("Empty CSV value is not supported: " + line);
            }
            values.add(part);
        }
        return values;
    }

    private static int resolveLabelColumn(String labelColumn, List<String> columnNames) {
        if (labelColumn == null || labelColumn.equalsIgnoreCase("last")) {
            return columnNames.size() - 1;
        }
        try {
            int index = Integer.parseInt(labelColumn);
            if (index < 0 || index >= columnNames.size()) {
                throw new IllegalArgumentException("Label column index out of range: " + labelColumn);
            }
            return index;
        } catch (NumberFormatException ignored) {
            for (int i = 0; i < columnNames.size(); i++) {
                if (columnNames.get(i).equals(labelColumn)) {
                    return i;
                }
            }
            throw new IllegalArgumentException("Label column name not found: " + labelColumn);
        }
    }

    private static EncodedDataset encodeCategoricalRows(List<String[]> rows,
                                                        List<String> columnNames,
                                                        int labelColumnIndex) {
        LinkedHashMap<FeatureKey, Integer> featureMap = new LinkedHashMap<>();
        for (String[] row : rows) {
            for (int columnIndex = 0; columnIndex < row.length; columnIndex++) {
                if (columnIndex == labelColumnIndex) {
                    continue;
                }
                FeatureKey key = new FeatureKey(columnIndex, row[columnIndex]);
                featureMap.computeIfAbsent(key, ignored -> featureMap.size());
            }
        }

        List<String> featureNames = new ArrayList<>();
        for (FeatureKey key : featureMap.keySet()) {
            featureNames.add(columnNames.get(key.columnIndex()) + "=" + key.value());
        }

        List<int[]> featureMatrix = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (String[] row : rows) {
            int[] encoded = new int[featureNames.size()];
            for (int columnIndex = 0; columnIndex < row.length; columnIndex++) {
                if (columnIndex == labelColumnIndex) {
                    labels.add(row[columnIndex]);
                    continue;
                }
                Integer featureIndex = featureMap.get(new FeatureKey(columnIndex, row[columnIndex]));
                if (featureIndex == null) {
                    throw new IllegalStateException("Missing feature index for " + columnNames.get(columnIndex) + "=" + row[columnIndex]);
                }
                encoded[featureIndex] = 1;
            }
            featureMatrix.add(encoded);
        }
        return new EncodedDataset(featureMatrix, labels, featureNames);
    }

    private static void writeRawCopy(Path dataPath, Path columnPath, Path outputDir, String dataset) throws IOException {
        Files.copy(dataPath, outputDir.resolve(dataset + ".csv"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(columnPath, outputDir.resolve(dataset + "_col.csv"), StandardCopyOption.REPLACE_EXISTING);
    }

    private static void writeFeatureMatrix(Path path, List<int[]> featureMatrix) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (int[] row : featureMatrix) {
                writeIntRow(writer, row, false);
            }
        }
    }

    private static void writeContext(Path path, List<int[]> featureMatrix) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(featureMatrix.size() + "," + featureMatrix.get(0).length);
            writer.newLine();
            for (int[] row : featureMatrix) {
                writeIntRow(writer, row, true);
            }
        }
    }

    private static void writeIntRow(BufferedWriter writer, int[] row, boolean trailingComma) throws IOException {
        for (int i = 0; i < row.length; i++) {
            writer.write(Integer.toString(row[i]));
            if (trailingComma || i < row.length - 1) {
                writer.write(",");
            }
        }
        writer.newLine();
    }

    private static void writeLabels(Path path, List<String> labels) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (String label : labels) {
                writer.write(label);
                writer.newLine();
            }
        }
    }

    private static void writeLabelMapping(Path path, List<String> labels) throws IOException {
        Set<String> orderedLabels = new LinkedHashSet<>(labels);
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("label_id,label");
            writer.newLine();
            int id = 0;
            for (String label : orderedLabels) {
                writer.write(id + "," + label);
                writer.newLine();
                id++;
            }
        }
    }

    private static void writeFeatureColumns(Path path, List<String> featureNames) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(String.join(",", featureNames));
            writer.newLine();
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

    private static ArrayList<AEConcept_id> buildCoreNegativeLattice(Context context,
                                                                    int splitDepth,
                                                                    int threads) {
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
        PICbo53C_ae.PICbo53C_ae_exe(context, startConcept, 1, new BitSet(), splitDepth, splitQueue, concepts);

        int workerCount = Math.max(1, threads);
        Map<Integer, Queue<aeConceptItem>> workQueues = new HashMap<>();
        Map<Integer, Queue<AEConcept>> partialResults = new HashMap<>();
        for (int i = 0; i < workerCount; i++) {
            workQueues.put(i, new LinkedList<>());
            partialResults.put(i, new LinkedList<>());
        }

        int splitCount = splitQueue.size();
        for (int i = 0; i < splitCount; i++) {
            workQueues.get(i % workerCount).offer(splitQueue.poll());
        }

        ExecutorService pool = Executors.newFixedThreadPool(workerCount);
        ArrayList<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < workerCount; i++) {
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
        for (int i = 0; i < workerCount; i++) {
            for (AEConcept concept : partialResults.get(i)) {
                result.add(new AEConcept_id(id++, concept.getExtent(), concept.getExtent_n(), concept.getIntent()));
            }
        }
        return result;
    }

    private static void buildCoreNegativeLatticePartition(Context context,
                                                         Queue<aeConceptItem> workQueue,
                                                         Queue<AEConcept> resultQueue) {
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

    private static void writeObjectConceptBipartite(Path outputDir,
                                                    String dataset,
                                                    Context context,
                                                    ArrayList<Concept> positiveConcepts,
                                                    ArrayList<AEConcept_id> negativeConcepts,
                                                    int profileBins) throws IOException {
        ObjectConceptBipartiteExporter.exportPositive(
                positiveConcepts,
                context.getObjs_size(),
                context.getAttrs_size(),
                profileBins,
                outputDir.resolve(dataset + "_positive_object_concept").toString());
        ObjectConceptBipartiteExporter.exportNegative(
                negativeConcepts,
                context.getObjs_size(),
                context.getAttrs_size(),
                profileBins,
                outputDir.resolve(dataset + "_negative_object_concept").toString());
        gzipAndDelete(outputDir.resolve(dataset + "_positive_object_concept_edges.csv"));
        gzipAndDelete(outputDir.resolve(dataset + "_negative_object_concept_edges.csv"));
    }

    private static void gzipAndDelete(Path csvPath) throws IOException {
        Path gzPath = Path.of(csvPath.toString() + ".gz");
        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(csvPath));
             GZIPOutputStream gzip = new GZIPOutputStream(new BufferedOutputStream(Files.newOutputStream(gzPath)))) {
            in.transferTo(gzip);
        }
        Files.delete(csvPath);
    }

    private static void writeManifest(Path path,
                                      Config config,
                                      Path outputDir,
                                      List<String> columnNames,
                                      int labelColumnIndex,
                                      EncodedDataset encoded,
                                      ArrayList<Concept> positiveConcepts,
                                      ArrayList<AEConcept_id> negativeConcepts) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("{\n");
            writer.write("  \"dataset\": \"" + escapeJson(config.dataset) + "\",\n");
            writer.write("  \"input_dir\": \"" + escapeJson(outputDir.toString()) + "\",\n");
            writer.write("  \"label_column_index\": " + labelColumnIndex + ",\n");
            writer.write("  \"label_column_name\": \"" + escapeJson(columnNames.get(labelColumnIndex)) + "\",\n");
            writer.write("  \"rows\": " + encoded.featureMatrix.size() + ",\n");
            writer.write("  \"feature_columns\": " + encoded.featureNames.size() + ",\n");
            writer.write("  \"profile_bins\": " + config.profileBins + ",\n");
            writer.write("  \"skip_adjacency\": " + config.skipAdjacency + ",\n");
            writer.write("  \"positive_concepts\": " + positiveConcepts.size() + ",\n");
            writer.write("  \"negative_concepts\": " + negativeConcepts.size() + "\n");
            writer.write("}\n");
        }
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private record EncodedDataset(List<int[]> featureMatrix, List<String> labels, List<String> featureNames) {
    }

    private record FeatureKey(int columnIndex, String value) {
    }

    private static class Config {
        private String dataset;
        private Path inputDir;
        private Path outputRoot;
        private String labelColumn = "last";
        private int profileBins = DEFAULT_PROFILE_BINS;
        private int aeSplitDepth = DEFAULT_AE_SPLIT_DEPTH;
        private int aeThreads = DEFAULT_AE_THREADS;
        private boolean skipAdjacency = false;

        private static Config parse(String[] args) {
            Map<String, String> values = new HashMap<>();
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.equals("--help") || arg.equals("-h")) {
                    printUsageAndExit();
                }
                if (!arg.startsWith("--")) {
                    throw new IllegalArgumentException("Unexpected argument: " + arg);
                }
                if (i + 1 >= args.length || args[i + 1].startsWith("--")) {
                    throw new IllegalArgumentException("Missing value for " + arg);
                }
                values.put(arg.substring(2), args[++i]);
            }

            Config config = new Config();
            config.dataset = required(values, "dataset");
            config.inputDir = Path.of(required(values, "input-dir")).toAbsolutePath().normalize();
            config.outputRoot = Path.of(required(values, "output-dir")).toAbsolutePath().normalize();
            config.labelColumn = values.getOrDefault("label-column", config.labelColumn);
            config.profileBins = parseInt(values, "profile-bins", config.profileBins);
            config.aeSplitDepth = parseInt(values, "ae-split-depth", config.aeSplitDepth);
            config.aeThreads = parseInt(values, "ae-threads", config.aeThreads);
            config.skipAdjacency = parseBoolean(values, "skip-adjacency",
                    parseBoolean(values, "skip", config.skipAdjacency));
            return config;
        }

        private static String required(Map<String, String> values, String key) {
            String value = values.get(key);
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Missing required argument --" + key);
            }
            return value;
        }

        private static int parseInt(Map<String, String> values, String key, int defaultValue) {
            String value = values.get(key);
            return value == null ? defaultValue : Integer.parseInt(value);
        }

        private static boolean parseBoolean(Map<String, String> values, String key, boolean defaultValue) {
            String value = values.get(key);
            if (value == null) {
                return defaultValue;
            }
            return switch (value.toLowerCase()) {
                case "true", "1", "yes", "y" -> true;
                case "false", "0", "no", "n" -> false;
                default -> throw new IllegalArgumentException("Invalid boolean value for --" + key + ": " + value);
            };
        }

        private static void printUsageAndExit() {
            System.out.println("Usage: java fca.experiment.BuildCategoricalExperimentInput \\");
            System.out.println("  --dataset car \\");
            System.out.println("  --input-dir /abs/path/to/raw_csv_folder \\");
            System.out.println("  --output-dir /abs/path/to/experiment_inputs \\");
            System.out.println("  [--label-column last|class|6] [--profile-bins 8] [--ae-split-depth 3] [--ae-threads 16] \\");
            System.out.println("  [--skip-adjacency true|false]");
            System.exit(0);
        }
    }
}
