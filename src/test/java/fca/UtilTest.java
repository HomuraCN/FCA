package fca;

import fca.utils.Context;
import fca.utils.readFile.File;
import fca.utils.util;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.BitSet;

public class UtilTest {
    @Test
    void testGetObjsShared() {
        try {
            Context context = File.readFile("src/main/java/data/context/car.data.txt");
            BitSet bitSet = new BitSet(1728);
            bitSet.set(2);
            BitSet objsShared = util.get_objs_shared(context, bitSet);
            System.out.println(objsShared);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
