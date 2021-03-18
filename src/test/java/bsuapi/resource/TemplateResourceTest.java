package bsuapi.resource;

import bsuapi.dbal.Cypher;
import bsuapi.test.TestCypherResource;
import bsuapi.test.TestJsonResource;
import org.json.JSONArray;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import static org.junit.Assert.*;

public class TemplateResourceTest
{
    protected static TestJsonResource j;
    protected static TestCypherResource db;

    @BeforeClass
    public static void setUp() {
        db = new TestCypherResource("mockGraph");
    }

    @AfterClass
    public static void tearDown() {
        db.close();
    }

    @Test
    public void folderListTest()
    throws Exception
    {
        TemplateResource resource = new TemplateResource();

        try (
                Transaction tx = db.beginTx()
        ) {
            Cypher c = db.createCypher();

            JSONArray result = resource.getFolderList(c);

            assertEquals("A", (result.query("/0/guid")));
            assertEquals("B", (result.query("/1/guid")));

            tx.success();
        }
    }
}
