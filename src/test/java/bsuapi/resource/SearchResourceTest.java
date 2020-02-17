package bsuapi.resource;

import bsuapi.behavior.*;
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

public class SearchResourceTest
{
    protected static TestJsonResource j;
    protected static TestCypherResource db;

    @BeforeClass
    public static void setUp() {
        db = new TestCypherResource("mockGraph");
        j = new TestJsonResource("requestTestParams");
        try (Transaction tx = db.beginTx()) {
            TestCypherResource.db.execute("CALL db.index.fulltext.createNodeIndex(\"topicNameIndex\",[\"Artist\",\"Classification\",\"Culture\",\"Nation\",\"Tag\"],[\"name\"])");
            TestCypherResource.db.execute("CALL db.index.fulltext.createNodeIndex(\"assetNameIndex\",[\"Artwork\"],[\"name\",\"title\"])");
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
        Request request = new Request(j.mockUriInfo("empty"));
        Response response = Response.prepare(request);
        String searchQuery ="Degas";

        Map<String, String> params = new HashMap<>();
        params.put(Search.searchParam, searchQuery);
//        response.setSearch(search);

        JSONObject result;
        String message;

        try (
            Transaction tx = db.beginTx()
        ) {
            Cypher c = db.createCypher();
            Behavior b = new TopicIndex(params);
            b.resolveBehavior(c);

            result = b.toJson();
            message = b.getMessage();
            tx.success();
        }

        assertNotNull(result);
        assertNotNull(message);
    }

    @Test
    public void searchCompositeIntegrationTest()
            throws Exception
    {
        String searchQuery ="Degas";

        Map<String, String> params = new HashMap<>();
        params.put(Search.searchParam, searchQuery);

        JSONObject result;
        String message;

        try (
                Transaction tx = db.beginTx()
        ) {
            Cypher c = db.createCypher();

            Behavior search = new Search(params);
            Behavior a = new TopicIndex(params);
            Behavior b = new AssetIndex(params);
            search.appendBehavior(a);
            search.appendBehavior(b);

            search.resolveBehavior(c);

            result = search.toJson();
            message = search.getMessage();
            tx.success();
        }

        assertNotNull(result);
        assertNotNull(message);
    }

    @Test
    public void searchAssets() {
        JSONObject responseData = this.search("Saint", "empty");

        assertTrue((Boolean) responseData.query("/success"));
        assertNotNull(responseData.query("/data/asset-results"));
        assertNotNull(responseData.query("/data/topic-results"));
        assertNotNull(responseData.query("/data/asset-results/results/0/searchScore"));
        assertEquals(1, ((JSONArray) responseData.query("/data/asset-results/results")).length());
        assertEquals(1, (responseData.query("/data/asset-results/resultCount")));
    }

    @Test
    public void searchDegas() {
        JSONObject responseData = this.search("Degas", "empty");

        assertTrue((Boolean) responseData.query("/success"));
        assertNotNull(responseData.query("/data/asset-results"));
        assertNotNull(responseData.query("/data/topic-results"));
        assertNotNull(responseData.query("/data/topic-results/results/0/searchScore"));
        assertEquals(5, ((JSONArray) responseData.query("/data/topic-results/results")).length());
        assertEquals(5, (responseData.query("/data/topic-results/resultCount")));
    }

    @Test
    public void searchDegasLimit() {
        JSONObject responseData = this.search("Degas", "limit2Page1");

        assertTrue((Boolean) responseData.query("/success"));
        assertNotNull(responseData.query("/data/asset-results"));
        assertNotNull(responseData.query("/data/topic-results"));
        assertNotNull(responseData.query("/data/topic-results/results/0/searchScore"));
        assertEquals(2, ((JSONArray) responseData.query("/data/topic-results/results")).length());
        assertEquals(5, (responseData.query("/data/topic-results/resultCount")));
    }

    @Test
    public void searchDegasPage() {
        JSONObject responseData = this.search("Degas", "limit2Page3");

        assertTrue((Boolean) responseData.query("/success"));

        assertNotNull(responseData.query("/data/asset-results"));
        assertNotNull(responseData.query("/data/topic-results"));
        assertNotNull(responseData.query("/data/topic-results/results/0/searchScore"));
        assertEquals(1, ((JSONArray) responseData.query("/data/topic-results/results")).length());
        assertEquals(5, (responseData.query("/data/topic-results/resultCount")));
    }

    // @todo: test malformed

    private JSONObject search(String query, String paramSet)
    {
        UriInfo uriInfo = j.mockUriInfo(paramSet);
        SearchResource resource = new SearchResource();
        db.baseResourceInjection(resource);

        try (Transaction tx = db.beginTx()) {
            javax.ws.rs.core.Response result = resource.search(query, uriInfo);
            tx.success();

            assertEquals(200, result.getStatus());

            return new JSONObject(UTF8.decode((byte[]) result.getEntity()));
        }
    }
}
