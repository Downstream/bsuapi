package bsuapi.resource;

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
        JSONObject responseData = this.searchDegas("empty");

        assertTrue((Boolean) responseData.query("/success"));
        assertNotNull(responseData.query("/data/search-results/0/searchScore"));
        assertEquals(5, ((JSONArray) responseData.query("/data/search-results")).length());
        assertEquals(5, (responseData.query("/data/resultCount")));
    }

    @Test
    public void searchDegasLimit() {
        JSONObject responseData = this.searchDegas("limit2Page1");

        assertTrue((Boolean) responseData.query("/success"));
        assertNotNull(responseData.query("/data/search-results/0/searchScore"));
        assertEquals(2, ((JSONArray) responseData.query("/data/search-results")).length());
        assertEquals(5, (responseData.query("/data/resultCount")));
    }

    @Test
    public void searchDegasPage() {
        JSONObject responseData = this.searchDegas("limit2Page3");

        assertTrue((Boolean) responseData.query("/success"));
        assertNotNull(responseData.query("/data/search-results/0/searchScore"));
        assertEquals(1, ((JSONArray) responseData.query("/data/search-results")).length());
        assertEquals(5, (responseData.query("/data/resultCount")));
    }

    // @todo: test malformed

    private JSONObject searchDegas(String paramSet)
    {
        UriInfo uriInfo = j.mockUriInfo(paramSet);
        SearchResource resource = new SearchResource();
        db.baseResourceInjection(resource);

        try (Transaction tx = db.beginTx()) {
            javax.ws.rs.core.Response result = resource.search("Degas", uriInfo);
            tx.success();

            assertEquals(result.getStatus(), 200);

            return new JSONObject(UTF8.decode((byte[]) result.getEntity()));
        }
    }
}
