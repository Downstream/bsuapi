package bsuapi.obj;

import bsuapi.test.TestJsonResource;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.neo4j.cypher.internal.v3_5.util.symbols.TemporalTypes.date;

public class ObjectOpenPipeDateTest {
    protected static TestJsonResource j;

    @BeforeClass
    public static void setUp() {
        j = new TestJsonResource("openPipeDateTest");
    }

    @AfterClass
    public static void tearDown()
    {
        j.close();
    }

    // BC 500 JAN 01 00:00:00  --> -500-01-01
    // CE 1927 FEB 13 23:59:59 -->  001927-02-13 23:59:59
    // Something else          -->  0-01-01
    // null                    -->  0-01-01

    @Test
    public void testOpenPipeDate() {
        ObjectOpenPipeDate testMe = new ObjectOpenPipeDate();

        JSONObject tests = j.getDoc();
        for (Iterator<String> it = tests.keys(); it.hasNext(); ) {
            String key = it.next();
            JSONObject val = (JSONObject) tests.get(key);
            assertEquals(val.toMap(), testMe.openPipeDateMap(key));
        }
    }

    @Test
    public void testOpenPipeDateNull() {
        ObjectOpenPipeDate testMe = new ObjectOpenPipeDate();
        JSONObject expect = (JSONObject) j.query("/unexpected");
        assertEquals(expect.toMap(), testMe.openPipeDateMap(null));
    }
}
