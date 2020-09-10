package bsuapi.obj;

import org.junit.Test;

import java.util.*;
import static org.junit.Assert.*;

public class ObjectOpenPipeFunctionTest {

    @Test
    public void openPipeCleanObjNormal() {
        Map m = this.testMap();
        List invalid = this.list("OpenPipe Canonical Default");

        ObjectOpenPipeFunction c = new ObjectOpenPipeFunction();
        Map result = c.openPipeCleanObj(m, invalid);

        Map expected = this.testMap();
        expected.remove("http://mec402.boisestate.edu/cgi-bin/openpipe/data/artist/-1");

        assertEquals(expected, result);
    }

    @Test
    public void openPipeCleanObjEmpty() {
        Map<String, String> m = new HashMap<>();
        List invalid = this.list("OpenPipe Canonical Default");

        ObjectOpenPipeFunction c = new ObjectOpenPipeFunction();
        Map result = c.openPipeCleanObj(m, invalid);

        assertNull(result);
    }

    @Test
    public void openPipeCleanObjAllRemoved() {
        Map m = this.testMap();
        m.remove("http://mec402.boisestate.edu/cgi-bin/openpipe/data/artist/70");

        List invalid = this.list("Blah");

        ObjectOpenPipeFunction c = new ObjectOpenPipeFunction();
        Map result = c.openPipeCleanObj(m, invalid);

        assertNull(result);
    }

    private Map testMap() {
        Map<String, String> m = new HashMap<>();
        m.put("http://mec402.boisestate.edu/cgi-bin/openpipe/data/artist/70", "Robert S. Duncanson (American, 1821-1872)");
        m.put("http://mec402.boisestate.edu/cgi-bin/openpipe/data/artist/72", "Blah");
        m.put("http://mec402.boisestate.edu/cgi-bin/openpipe/data/artist/-1", "OpenPipe Canonical Default");

        return m;
    }

    private ArrayList<Object> list(Object ... a)
    {
        return new ArrayList<>(Arrays.asList(a));
    }
}
