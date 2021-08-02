package bsuapi.dbal.query;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.NodeType;
import bsuapi.dbal.Topic;
import bsuapi.test.TestCypherResource;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TimelineManetTest {

    protected static TestCypherResource db;
    protected static Cypher c;

    @BeforeClass
    public static void setUp() {
        db = new TestCypherResource("timelineManet");
        c = db.createCypher();
    }

    @AfterClass
    public static void tearDown() {
        c.close();
        db.close();
    }

    @Test
    public void manetResultTest() throws CypherException {
        Timeline t = this.createTimeline();
        c.query(t);
        JSONObject result = t.getResults();
        assertNotNull(result);
        assertEquals("100/6417", result.query("/1880/0/keyRaw"));
        assertEquals("100/6412", result.query("/1866/0/keyRaw"));
        assertEquals("100/6415", result.query("/1866/1/keyRaw"));

    }

    private Timeline createTimeline() throws CypherException {
        Topic topic = new Topic(NodeType.FOLDER, "MANET");
        c.resolveNode(topic);
        return new Timeline(topic);
    }
}
