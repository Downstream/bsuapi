package bsuapi.test;

import apoc.util.Util;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import static org.junit.Assert.assertEquals;

abstract public class BaseJsonTest {
    protected static JSONObject j;

    @BeforeClass
    public static void setUp()
    {
        BaseJsonTest.preLoadJsonResource("metAsset");
    }

    @AfterClass
    public static void tearDown()
    {
        BaseJsonTest.closeResource();
    }

    public void testJsonLoaded()
    {
        this.queryAssert("/objectID", "436533");
        this.queryAssert("/constituents/0/name", "Vincent van Gogh");
        this.queryAssert("/tags/0", "Shoes");
    }

    public static void preLoadJsonResource(String resourceName)
    {
        j = new JSONObject(Util.readResourceFile(resourceName+".json"));
    }

    public static void closeResource()
    {
        j = null;
    }

    protected void queryAssert(String jsonPointer, String expected)
    {
        assertEquals(this.strQuery(jsonPointer), expected);
    }

    protected Object query(String jsonPointer)
    {
        return j.query(jsonPointer);
    }

    protected String strQuery(String jsonPointer)
    {
        return this.query(jsonPointer).toString();
    }
}
