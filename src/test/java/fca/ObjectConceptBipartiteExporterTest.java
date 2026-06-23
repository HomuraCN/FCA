package fca;

import fca.algorithm.bipartite.ObjectConceptBipartiteExporter;
import fca.algorithm.encoding.ConceptPositionEncoder;
import fca.utils.Context;
import fca.utils.concept.AEConcept_id;
import fca.utils.concept.Concept;
import fca.utils.readFile.AELatticeFileHandler;
import fca.utils.readFile.File;
import fca.utils.readFile.LatticeFileHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

public class ObjectConceptBipartiteExporterTest {

    @Test
    void testExportObjectConceptBipartiteGraph() {
        try {
            String datasetName = "iris"; // 需要切换数据集时，修改这里即可，例如 car、zoo、nursery
            int profileBins = ConceptPositionEncoder.DEFAULT_DEPTH_PROFILE_BINS;

            Context context = File.readFile("src/main/java/data/context/" + datasetName + ".data.txt");
            int objectCount = context.getObjs_size();
            int attributeCount = context.getAttrs_size();

            ArrayList<Concept> positiveConcepts = LatticeFileHandler.readLatticeFromFile(
                    "src/main/java/data/lattice/" + datasetName + "_lattice.data.txt");
            ArrayList<AEConcept_id> negativeConcepts = AELatticeFileHandler.readAELatticeFromFile(
                    "src/main/java/data/lattice3C/" + datasetName + "_aelattice.data.txt");

            String outputDir = "src/main/java/data/bipartite/";
            ObjectConceptBipartiteExporter.exportPositive(
                    positiveConcepts,
                    objectCount,
                    attributeCount,
                    profileBins,
                    outputDir + datasetName + "_positive_object_concept");
            ObjectConceptBipartiteExporter.exportNegative(
                    negativeConcepts,
                    objectCount,
                    attributeCount,
                    profileBins,
                    outputDir + datasetName + "_negative_object_concept");

            System.out.println(datasetName + " 对象-概念二部图导出完成。");
            System.out.println("对象数: " + objectCount);
            System.out.println("属性数: " + attributeCount);
            System.out.println("正概念数: " + positiveConcepts.size());
            System.out.println("负概念数: " + negativeConcepts.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
