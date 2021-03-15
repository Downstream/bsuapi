package bsuapi.resource;

import bsuapi.dbal.script.CypherScript;
import bsuapi.test.TestCypherResource;
import bsuapi.test.TestJsonResource;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.string.UTF8;

import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.*;

public class ExecutorResourceTest
{
    protected static TestJsonResource j;
    protected static TestCypherResource db;

    @BeforeClass
    public static void setUp() {
        db = new TestCypherResource("mockGraph");
        j = new TestJsonResource("requestTestParams");
    }

    @AfterClass
    public static void tearDown() {
        db.close();
        j.close();
    }

    @Test
    public void infoCardGeneration() {
        UriInfo uriInfo = j.mockUriInfo("empty");
        ExecutorResource resource = new ExecutorResource();
        db.baseResourceInjection(resource);
        javax.ws.rs.core.Response result = resource.start(CypherScript.INFO.name(), uriInfo);

        assertEquals(result.getStatus(), 200);

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));

        assertTrue((Boolean) responseData.query("/success"));
        assertEquals("Starting or checking CypherScript: INFO", responseData.query("/message").toString());
    }

    @Test
    public void reset() {
        UriInfo uriInfo = j.mockUriInfo("empty");
        ExecutorResource resource = new ExecutorResource();
        db.baseResourceInjection(resource);
        javax.ws.rs.core.Response result = resource.start(CypherScript.OPENPIPE_RESET.name(), uriInfo);

        assertEquals(result.getStatus(), 200);

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));

        assertTrue((Boolean) responseData.query("/success"));
        assertEquals("Starting or checking CypherScript: OPENPIPE_RESET", responseData.query("/message").toString());
    }
}
