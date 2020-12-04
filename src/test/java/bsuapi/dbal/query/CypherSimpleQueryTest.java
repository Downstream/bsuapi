package bsuapi.dbal.query;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.Node;
import bsuapi.dbal.NodeType;
import bsuapi.resource.RelatedResource;
import bsuapi.test.TestCypherResource;
import bsuapi.test.TestJsonResource;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.string.UTF8;

import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.*;

public class CypherSimpleQueryTest
{
    protected static TestCypherResource db;

    @BeforeClass
    public static void setUp() {
        db = new TestCypherResource("syncConfig");
    }

    @AfterClass
    public static void tearDown() {
        db.close();
    }

    @Test
    public void testGetApiConfigFromDB()
    throws Throwable
    {
        Cypher c = this.db.createCypher();

        Node node = c.querySingleNode("MATCH (api:OpenPipeConfig {name: 'api'}) RETURN api", "api");

        assertNotNull(node);
        assertEquals("api", node.getRawProperty("name"));
    }
}
