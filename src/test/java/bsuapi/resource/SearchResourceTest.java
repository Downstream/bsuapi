package bsuapi.resource;

import bsuapi.test.TestCypherResource;
import bsuapi.test.TestJsonResource;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;
import org.neo4j.string.UTF8;

import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.*;

public class SearchResourceTest
{
    protected static TestJsonResource j;
    protected static TestCypherResource db;

    @BeforeClass
    public static void setUp() {
        db = new TestCypherResource("mockGraph");
        j = new TestJsonResource("requestTestParams");
        try (Transaction tx = db.beginTx()) {
            TestCypherResource.db.execute("CALL db.index.fulltext.createNodeIndex(\"nameIndex\",[\"Artist\",\"Classification\",\"Culture\",\"Nation\",\"Tag\"],[\"name\"])");
            tx.success();
        }
    }

    @AfterClass
    public static void tearDown() {
        db.close();
        j.close();
    }

    @Test
    public void searchDegas() {
        UriInfo uriInfo = j.mockUriInfo("empty");
        SearchResource resource = new SearchResource();
        db.baseResourceInjection(resource);
        javax.ws.rs.core.Response result = resource.search(uriInfo, "Degas");

        assertEquals(result.getStatus(), 200);

        JSONObject responseData = new JSONObject(UTF8.decode((byte[]) result.getEntity()));

        assertTrue((Boolean) responseData.query("/success"));
        assertEquals("infoCards.cypher graph generated", responseData.query("/message").toString());
        assertNotNull(responseData.query("/data/clear/0"));
        assertNotNull(responseData.query("/data/create/0"));
    }

    // @todo: test limit
    // @todo: test page
    // @todo: test malformed
}
