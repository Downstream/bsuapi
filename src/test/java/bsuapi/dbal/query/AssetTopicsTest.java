package bsuapi.dbal.query;

import bsuapi.dbal.*;
import bsuapi.test.TestCypherResource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AssetTopicsTest {

    protected static TestCypherResource db;
    protected static Cypher c;

    @BeforeClass
    public static void setUp() {
        db = new TestCypherResource("mockGraph");
        c = db.createCypher();
    }

    @AfterClass
    public static void tearDown() {
        c.close();
        db.close();
    }

    @Test
    public void getResults() throws CypherException {
        AssetTopics query = this.createQuery(NodeType.ARTIST);
        JSONArray result = query.exec(c);

        assertEquals("DEGAS", result.query("/0/keyRaw"));

    }

    private AssetTopics createQuery(NodeType type) throws CypherException {
        Asset asset = new Asset("ASSET");
        c.resolveNode(asset);
        return new AssetTopics(asset, type);
    }
}
