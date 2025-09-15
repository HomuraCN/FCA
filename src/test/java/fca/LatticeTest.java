package fca;

import fca.utils.concept.Concept;
import fca.utils.readFile.LatticeFileHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

public class LatticeTest {
    @Test
    void testLattice() {
        try {
            ArrayList<Concept> concepts = LatticeFileHandler.readLatticeFromFile("src/main/java/data/lattice/car_lattice.data.txt");
            for (int i = 0; i < 10; i++) {
                Concept concept = concepts.get(i);
                System.out.println(concept);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
