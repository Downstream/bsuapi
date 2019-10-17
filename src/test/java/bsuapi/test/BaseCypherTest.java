package bsuapi.test;

import apoc.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;

abstract public class BaseCypherTest {
    protected static GraphDatabaseService db;

    @BeforeClass
    public static void setUp()
    {
        BaseCypherTest.preloadCypherResource("mockGraph");
    }

    @AfterClass
    public static void tearDown()
    {
        BaseCypherTest.closeResource();
    }

    @Test
    public void testGraphLoaded()
    {
        try (Transaction tx = db.beginTx()) {
            Node n = this.queryOne("c", "MATCH (c:Person {name: 'Keanu Reeves'}) RETURN c");
            assertEquals(n.getProperty("name"), "Keanu Reeves");
            assertEquals(n.getProperty("born"), 1964L);
        }
    }

    public static void preloadCypherResource(String resourceName)
    {
        db = new TestGraphDatabaseFactory().newImpermanentDatabase();
        String movies = Util.readResourceFile(resourceName+".cypher");
        try (Transaction tx = db.beginTx()) {
            db.execute(movies);
            tx.success();
        }
    }

    public static void closeResource()
    {
        if (null != db) {
            db.shutdown();
        }
    }

    protected Node queryOne(String column, String query)
    {
        Node n;
        int matchCount = 0;
        Iterator<Node> it = this.query(column, query);
        n = it.next();
        matchCount++;
        while ( it.hasNext() ) {
            it.next();
            matchCount++;
        }

        assertEquals(matchCount, 1);
        return n;
    }

    protected Iterator<Node> query (String column, String query)
    {
        return db.execute(query).columnAs(column);
    }
}
