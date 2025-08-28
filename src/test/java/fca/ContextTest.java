package fca;

import fca.utils.Context;
import fca.utils.readFile.File;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.BitSet;
import java.util.Map;

public class ContextTest {
    @Test
    public void test() {
        try {
            Context context = File.readFile("src/main/java/data/context/car.data.txt");
            System.out.println(context.getObjs_size());
            System.out.println(context.getAttrs_size());
            Map<Integer, BitSet> objs = context.getObjs();
            Map<Integer, BitSet> attrs = context.getAttrs();
            Map<Integer, BitSet> objsN = context.getObjs_n();
            Map<Integer, BitSet> attrsN = context.getAttrs_n();
            System.out.println(objs.get(1));
            System.out.println(objsN.get(1));
            System.out.println(objs.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
