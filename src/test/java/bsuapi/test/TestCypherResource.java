package bsuapi.test;

import bsuapi.resource.Util;
import bsuapi.dbal.Cypher;
import bsuapi.resource.BaseResource;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.kernel.lifecycle.LifecycleException;
import org.neo4j.logging.Log;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

public class TestCypherResource implements AutoCloseable
{
    public static GraphDatabaseService db;

    private TestCypherResource() {};

    public TestCypherResource (String filename)
    {
        db = new TestGraphDatabaseFactory().newImpermanentDatabase();

        this.loadFileResource(filename);
    }

    public static TestCypherResource JaxWS (String filename)
    {
        /* can't do this and keep the db static */
//        TestCypherResource t = new TestCypherResource();
//        TestCypherResource.db = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder()
//            .setConfig(GraphDatabaseSettings.auth_enabled, String.valueOf(true))
//            .newGraphDatabase();
//
//        t.loadFileResource(filename);
//        return t;

        return new TestCypherResource(filename);
    }

    private void loadFileResource(String filename)
    {
        try (Transaction tx = db.beginTx()) {
            db.execute(Util.readResourceFile(filename+".cypher"));
            tx.success();
        } catch (Exception e) {
            fail("Couldn't load "+filename+".cypher: "+e.getMessage());
        }
    }

    public Transaction beginTx()
    {
        return db.beginTx();
    }

    public Cypher createCypher()
    {
        return new Cypher(db);
    }

    public Log mockLogger()
    {
        Log log = mock(Log.class);
        doNothing().when(log).debug(anyString());
        doNothing().when(log).info(anyString());
        doNothing().when(log).warn(anyString());
        doNothing().when(log).error(anyString());

        return log;
    }

    public void baseResourceInjection(BaseResource resource)
    {
        resource.db = TestCypherResource.db;
        resource.log = this.mockLogger();
    }

    public Node queryOne(String column, String query)
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

    public Iterator<Node> query (String column, String query)
    {
        return db.execute(query).columnAs(column);
    }

    @Override
    public void close()
    {
        if (null != db) {
            try {
                db.shutdown();
            } catch (LifecycleException ignore) {}
            // test implementations of fulltest index do not properly shutdown lucene
            // ... BUT if we get that error, it almost certainly means that Lifecycle manager is trying to close the index, while lucene has already been closed.
            // worst case, is the rare occasion of a memory leak when running tests - nothing a reboot can't fix.
        }
    }
}
