package fca;

import fca.algorithm.encoding.ConceptPositionEncoder;
import fca.algorithm.encoding.ConceptPositionEncoder.DepthAggregationMode;
import fca.utils.concept.Concept;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConceptPositionEncoderTest {

    @Test
    void computesLongestConceptDepthsFromPartialOrder() {
        ArrayList<Concept> concepts = sampleConcepts();

        int[] depths = ConceptPositionEncoder.computeConceptDepths(concepts);

        assertArrayEquals(new int[]{0, 1, 1, 2, 3}, depths);
    }

    @Test
    void aggregatesObjectDepthAndBreadth() {
        ArrayList<Concept> concepts = sampleConcepts();

        double[][] cpe = ConceptPositionEncoder.computeObjectCPE(concepts, 3);

        assertEquals(3, cpe.length);
        assertEquals(2, cpe[0].length);

        assertEquals(1.0 / 6.0, cpe[0][0], 1e-9);
        assertEquals(0.0, cpe[0][1], 1e-9);

        assertEquals(1.0 / 3.0, cpe[1][0], 1e-9);
        assertEquals(1.0, cpe[1][1], 1e-9);

        assertEquals(1.0 / 6.0, cpe[2][0], 1e-9);
        assertEquals(0.0, cpe[2][1], 1e-9);
    }

    @Test
    void supportsMaxDepthAggregation() {
        ArrayList<Concept> concepts = sampleConcepts();

        double[][] cpe = ConceptPositionEncoder.computeObjectCPE(concepts, 3, DepthAggregationMode.MAX);

        assertEquals(1.0 / 3.0, cpe[0][0], 1e-9);
        assertEquals(2.0 / 3.0, cpe[1][0], 1e-9);
        assertEquals(1.0 / 3.0, cpe[2][0], 1e-9);
    }

    @Test
    void supportsSupportWeightedMeanDepthAggregation() {
        ArrayList<Concept> concepts = sampleConcepts();

        double[][] cpe = ConceptPositionEncoder.computeObjectCPE(
                concepts,
                3,
                DepthAggregationMode.SUPPORT_WEIGHTED_MEAN);

        double topWeight = 1.0 / Math.log1p(3);
        double twoObjectConceptWeight = 1.0 / Math.log1p(2);
        double oneObjectConceptWeight = 1.0 / Math.log1p(1);

        double object1Depth = twoObjectConceptWeight / (topWeight + twoObjectConceptWeight);
        double object2Depth = (2 * twoObjectConceptWeight + 2 * oneObjectConceptWeight)
                / (topWeight + 2 * twoObjectConceptWeight + oneObjectConceptWeight);

        assertEquals(object1Depth / 3.0, cpe[0][0], 1e-9);
        assertEquals(object2Depth / 3.0, cpe[1][0], 1e-9);
        assertEquals(object1Depth / 3.0, cpe[2][0], 1e-9);
    }

    @Test
    void buildsDepthProfileEncoding() {
        ArrayList<Concept> concepts = sampleConcepts();

        double[][] cpe = ConceptPositionEncoder.computeObjectDepthProfileCPE(concepts, 3, 4);

        double topWeight = 1.0 / Math.log1p(3);
        double twoObjectConceptWeight = 1.0 / Math.log1p(2);
        double oneObjectConceptWeight = 1.0 / Math.log1p(1);

        double object1Total = topWeight + twoObjectConceptWeight;
        double object2Total = topWeight + 2 * twoObjectConceptWeight + oneObjectConceptWeight;

        assertEquals(5, cpe[0].length);

        assertEquals(topWeight / object1Total, cpe[0][0], 1e-9);
        assertEquals(twoObjectConceptWeight / object1Total, cpe[0][1], 1e-9);
        assertEquals(0.0, cpe[0][2], 1e-9);
        assertEquals(0.0, cpe[0][3], 1e-9);
        assertEquals(0.0, cpe[0][4], 1e-9);

        assertEquals(topWeight / object2Total, cpe[1][0], 1e-9);
        assertEquals((2 * twoObjectConceptWeight) / object2Total, cpe[1][1], 1e-9);
        assertEquals(oneObjectConceptWeight / object2Total, cpe[1][2], 1e-9);
        assertEquals(0.0, cpe[1][3], 1e-9);
        assertEquals(1.0, cpe[1][4], 1e-9);
    }

    private static ArrayList<Concept> sampleConcepts() {
        ArrayList<Concept> concepts = new ArrayList<>();
        concepts.add(concept(1, 1, 2, 3));
        concepts.add(concept(2, 1, 2));
        concepts.add(concept(3, 2, 3));
        concepts.add(concept(4, 2));
        concepts.add(concept(5));
        return concepts;
    }

    private static Concept concept(int id, int... objectIds) {
        BitSet extent = new BitSet();
        for (int objectId : objectIds) {
            extent.set(objectId);
        }
        return new Concept(extent, new BitSet(), id);
    }
}
