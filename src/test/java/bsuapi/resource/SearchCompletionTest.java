package bsuapi.resource;

import bsuapi.behavior.AssetIndex;
import bsuapi.behavior.Behavior;
import bsuapi.behavior.Search;
import bsuapi.behavior.TopicIndex;
import bsuapi.dbal.Cypher;
import bsuapi.test.TestCypherResource;
import bsuapi.test.TestJsonResource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;
import org.neo4j.string.UTF8;

import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class SearchCompletionTest
{
    protected static TestJsonResource j;
    protected static TestCypherResource db;

    @BeforeClass
    public static void setUp() {
        db = new TestCypherResource("mockGraph");
        j = new TestJsonResource("requestTestParams");
        try (Transaction tx = db.beginTx()) {
            TestCypherResource.db.execute("CALL db.index.fulltext.createNodeIndex(\"topicNameIndex\",[\"Artist\",\"Classification\",\"Culture\",\"Nation\",\"Tag\"],[\"name\"])");
            tx.success();
        }
    }

    @AfterClass
    public static void tearDown() {
        db.close();
        j.close();
    }

    @Test
    public void searchIndexIntegrationTest()
    throws Exception
    {
        JSONObject responseData = this.buildCompletion("Deg");

        assertEquals("Edgar Degas", responseData.query("/data/predictions/0"));
        assertEquals(7, (int) responseData.query("/data/count"));
        assertEquals(7, ((JSONArray) responseData.query("/data/predictions")).length());
        assertTrue((boolean) responseData.query("/success"));
    }

    // @todo: test malformed

    private JSONObject buildCompletion(String searchParam)
    {
        UriInfo uriInfo = j.mockUriInfo("empty");
        SearchResource resource = new SearchResource();
        db.baseResourceInjection(resource);

        try (Transaction tx = db.beginTx()) {
            javax.ws.rs.core.Response result = resource.complete(searchParam, uriInfo);
            tx.success();

            assertEquals(200, result.getStatus());

            return new JSONObject(UTF8.decode((byte[]) result.getEntity()));
        }
    }
}
