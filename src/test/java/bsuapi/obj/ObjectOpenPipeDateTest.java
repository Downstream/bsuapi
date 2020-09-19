package bsuapi.obj;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ObjectOpenPipeDateTest {

    // BC 500 JAN 01 00:00:00 --> -500-01-01 00:00:00
    // CE 1927 JAN 01 00:00:00 --> 1927-01-01 00:00:00

    @Test
    public void testOpenPipeCleanObj() {
        ObjectOpenPipeDate testMe = new ObjectOpenPipeDate();

        for (Map.Entry<String,String> entry : ObjectOpenPipeDateTest.tests.entrySet()) {
            assertEquals(entry.getValue(), testMe.openPipeDate(entry.getKey()));
        }
    }

    private static HashMap<String, String> tests;
    static {
        tests = new HashMap<>();
        tests.put("BC 500 JAN 01 00:00:00", "-500-01-01 00:00:00");
        tests.put("BC 50 JAN 01 00:00:00", "-50-01-01 00:00:00");
        tests.put("BC 599 JAN 01 00:00:00", "-599-01-01 00:00:00");
        tests.put("CE 1927 FEB 13 14:33:44", "1927-02-13 14:33:44");
        tests.put("CE 500 DEC 31 00:00:00", "500-12-31 00:00:00");
        tests.put("CE 2012 JUl 4 00:00:00", "2012-07-04 00:00:00");
        tests.put("CE 1272 NOV 25 00:00:00", "1272-11-25 00:00:00");
    }
}
