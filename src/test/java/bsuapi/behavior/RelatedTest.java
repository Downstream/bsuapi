package bsuapi.behavior;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.NodeType;
import bsuapi.dbal.Topic;
import bsuapi.test.TestCypherResource;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class RelatedTest
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
    public void integrationTestRelatedFromType()
    throws CypherException, BehaviorException
    {
        try (
                Transaction tx = db.beginTx();
                Cypher c = db.createCypher()
        ) {
            Behavior a = BehaviorType.RELATED.compose(c, Topic.plainMap(NodeType.ARTIST.labelName(), "Edgar Degas"));
            tx.success();

            assertEquals(a.getBehaviorKey(), "related");
            String msg = a.getMessage();
            assertTrue(msg.length() > 0);

            JSONObject data = (JSONObject) a.getBehaviorData();

            assertEquals("French", data.query("/Nation/0/name"));
            assertEquals("Drawings", data.query("/Classification/0/name"));
            assertEquals("Profiles", data.query("/Tag/0/name"));

            JSONObject result = a.toJson();

            assertEquals("Artist", result.query("/topic"));
            assertNotNull(result.query("/node/linkRelated"));
            assertEquals("Edgar Degas",result.query("/node/name"));

            assertEquals("French", result.query("/related/Nation/0/name"));
            assertEquals("Drawings", result.query("/related/Classification/0/name"));
            assertEquals("Profiles", result.query("/related/Tag/0/name"));

            assertEquals(334323L, result.query("/assets/0/objectID"));
            assertEquals("Profiles", ((String[]) result.query("/assets/0/tags"))[1]);
            assertEquals("Head of a Saint (profile to the right), after Fra Angelico", result.query("/assets/0/title").toString());
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
