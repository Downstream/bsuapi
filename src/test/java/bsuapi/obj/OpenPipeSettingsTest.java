package bsuapi.obj;

import bsuapi.test.TestJsonResource;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OpenPipeSettingsTest {
    protected static TestJsonResource j;

    @BeforeClass
    public static void setUp() {
        j = new TestJsonResource("openPipeSettingsTest");
    }

    @AfterClass
    public static void tearDown()
    {
        j.close();
    }

    @Test
    public void testOpenPipeColor() {
        OpenPipeSettings testMe = new OpenPipeSettings(this.getSettingSource("color"));

        assertEquals("color",testMe.name());
        assertTrue(testMe.data().has("colors"));
        assertTrue(testMe.isValid());
        assertEquals("Midnight",testMe.data().query("/colors/1/title"));
        assertTrue((Boolean) testMe.data().query("/colors/1/isColor"));
        assertEquals("Test Message",testMe.data().query("/message"));
    }

    @Test
    public void testOpenPipeTimeline() {
        OpenPipeSettings testMe = new OpenPipeSettings(this.getSettingSource("timeline"));

        assertEquals("timeline",testMe.name());
        assertTrue(testMe.data().has("preset"));
        assertTrue(testMe.isValid());
        assertEquals("Tag",testMe.data().query("/preset/3/topicType"));
    }

    private JSONObject getSettingSource(String name)
    {
        return (JSONObject) j.query("/" +name);
    }
}
