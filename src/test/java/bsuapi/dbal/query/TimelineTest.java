package bsuapi.dbal.query;

import bsuapi.dbal.*;
import bsuapi.test.TestCypherResource;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class TimelineTest {

    protected static TestCypherResource db;
    protected static Cypher c;

    @BeforeClass
    public static void setUp() {
        db = new TestCypherResource("timelineGraph");
        c = db.createCypher();
    }

    @AfterClass
    public static void tearDown() {
        c.close();
        db.close();
    }

    @Test
    public void getResults() throws CypherException {
        Timeline t = this.createTimeline();
        c.query(t);
        JSONObject result = t.getResults();
        assertNotNull(result);
        assertEquals("A", result.query("/0/0/keyRaw"));
        assertEquals("B", result.query("/940/0/keyRaw"));
        assertEquals("C", result.query("/2020/0/keyRaw"));

    }

    private Timeline createTimeline() throws CypherException {
        Topic topic = new Topic(NodeType.FOLDER, "F");
        c.resolveNode(topic);
        return new Timeline(topic);
    }
}
