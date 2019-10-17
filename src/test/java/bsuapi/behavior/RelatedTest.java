package bsuapi.behavior;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.NodeType;
import bsuapi.dbal.Topic;
import bsuapi.test.BaseCypherTest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import static org.junit.Assert.*;

public class RelatedTest extends BaseCypherTest {

    @Test
    public void integrationTestRelatedComposeArtist() throws CypherException {
        try (
                Transaction tx = db.beginTx();
                Cypher c = new Cypher(db)
        ) {
            Topic t = new Topic(NodeType.ARTIST.labelName(), "Edgar Degas");
            c.resolveTopic(t);
            Related a = new Related(t);
            a.resolveBehavior(c);

            assertEquals(a.getBehaviorKey(), "related");
            String msg = a.getMessage();
            assertTrue(msg.length() > 0);

            JSONObject data = (JSONObject) a.getBehaviorData();

            assertEquals("French", data.query("/Nation/0/name"));
            assertEquals("Drawings", data.query("/Classification/0/name"));
            assertEquals("Men", data.query("/Tag/0/name"));
        }
    }

    @Test
    public void integrationTestRelatedNotResolved() throws CypherException {
        try (
                Transaction tx = db.beginTx();
                Cypher c = new Cypher(db)
        ) {
            Topic t = new Topic(NodeType.ARTIST.labelName(), "Edgar Degas");
            c.resolveTopic(t);
            Related a = new Related(t);

            assertEquals("Related not Resolved.", a.getMessage());
            assertNull(a.getBehaviorData());
        }
    }

    @Test
    public void integrationTestRelatedTopicNotResolved() throws CypherException {
        try (
                Transaction tx = db.beginTx();
                Cypher c = new Cypher(db)
        ) {
            Topic t = new Topic(NodeType.ARTIST.labelName(), "Edgar Degas");
            Related a = new Related(t);
            a.resolveBehavior(c);

            assertEquals("No Match Found For :Artist", a.getMessage());
            assertNotNull(a.getBehaviorData());

            JSONObject behavior = a.toJson();
            assertNull(behavior.query("/node"));
        }
    }

    @Test
    public void integrationTestRelatedDescribe() {
        BehaviorDescribe desc = Related.describe();

        assertNotNull(desc.query("/uri"));
        assertNotNull(desc.query("/description"));
        assertNotNull(desc.query("/args/topic"));
        assertNotNull(desc.query("/args/value"));
    }
}
