package bsuapi.behavior;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.NodeType;
import bsuapi.dbal.Topic;
import bsuapi.test.TestCypherResource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import static org.junit.Assert.*;

public class AssetsTest
{
    protected static TestCypherResource db;

    @BeforeClass
    public static void setUp() {
        db = new TestCypherResource("mockGraph");
    }

    @AfterClass
    public static void tearDown()
    {
        db.close();
    }

    @Test
    public void integrationTestAssetsComposeArtist() throws CypherException {
        try (
                Transaction tx = db.beginTx();
                Cypher c = db.createCypher()
        ) {
            Topic t = new Topic(NodeType.ARTIST.labelName(), "Edgar Degas");
            c.resolveTopic(t);
            Assets a = new Assets(t);
            a.resolveBehavior(c);
            tx.success();

            assertEquals(a.getBehaviorKey(), "assets");
            String msg = a.getMessage();
            assertTrue(msg.length() > 0);

            JSONArray data = (JSONArray) a.getBehaviorData();
            JSONObject asset = (JSONObject) data.get(0);

            assertEquals(334323L, asset.query("/objectID"));
            assertEquals("Profiles", ((String[]) asset.query("/tags"))[1]);
            assertEquals("Head of a Saint (profile to the right), after Fra Angelico", asset.query("/title").toString());
        }
    }

    @Test
    public void integrationTestAssetsNotResolved() throws CypherException {
        try (
                Transaction tx = db.beginTx();
                Cypher c = db.createCypher()
        ) {
            Topic t = new Topic(NodeType.ARTIST.labelName(), "Edgar Degas");
            c.resolveTopic(t);
            Assets a = new Assets(t);
            tx.success();

            assertEquals("Assets not Resolved.", a.getMessage());
            assertNull(a.getBehaviorData());
        }
    }

    @Test
    public void integrationTestAssetsTopicNotResolved() throws CypherException {
        try (
                Transaction tx = db.beginTx();
                Cypher c = db.createCypher()
        ) {
            Topic t = new Topic(NodeType.ARTIST.labelName(), "Edgar Degas");
            Assets a = new Assets(t);
            a.resolveBehavior(c);
            tx.success();

            assertEquals("No Match Found For :Artist", a.getMessage());
            assertNotNull(a.getBehaviorData());

            JSONObject behavior = a.toJson();
            assertNull(behavior.query("/node"));
        }
    }

    @Test
    public void integrationTestRelatedFromType() throws CypherException {
        try (
                Transaction tx = db.beginTx();
                Cypher c = db.createCypher()
        ) {
            Topic t = new Topic(NodeType.ARTIST.labelName(), "Edgar Degas");
            Behavior a = BehaviorType.ASSETS.compose(t, c);
            tx.success();

            assertEquals(a.getBehaviorKey(), "assets");
            String msg = a.getMessage();
            assertTrue(msg.length() > 0);

            JSONArray data = (JSONArray) a.getBehaviorData();
            JSONObject asset = (JSONObject) data.get(0);

            assertEquals(334323L, asset.query("/objectID"));
            assertEquals("Profiles", ((String[]) asset.query("/tags"))[1]);
            assertEquals("Head of a Saint (profile to the right), after Fra Angelico", asset.query("/title").toString());
        }
    }

    @Test
    public void integrationTestAssetsDescribe() {
        BehaviorDescribe desc = Related.describe();

        assertNotNull(desc.query("/uri"));
        assertNotNull(desc.query("/description"));
        assertNotNull(desc.query("/args/topic"));
        assertNotNull(desc.query("/args/value"));
    }
}
