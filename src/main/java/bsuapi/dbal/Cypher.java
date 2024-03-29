package bsuapi.dbal;

import bsuapi.dbal.query.QueryResultCollector;
import bsuapi.dbal.query.QueryResultSingleColumn;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class Cypher implements AutoCloseable
{
    private GraphDatabaseService db;

    public Cypher(GraphDatabaseService db)
    {
        this.db = db;
    }

    public void resolveNode(Element element)
    throws CypherException
    {
        // @todo pick by (curator/algo) score
        element.setNode(this.findOne(element.getType(), element.getNodeKeyField(), element.getNodeKey()));
    }

    public Node findOne(NodeType type, String keyName, String keyVal)
    throws CypherException
    {
        Node result = null;

        try (
                Transaction tx = db.beginTx();
                ResourceIterator<org.neo4j.graphdb.Node> matches = db.findNodes(type.label(), keyName, keyVal)
        ) {
            ArrayList<Node> matchNodes = new ArrayList<>();
            if ( matches.hasNext() ) {
                result = new Node(matches.next());
            }

            tx.success();
            matches.close();
            if (result == null) {
                throw new CypherException("Cypher.findAll unable to retrieve matches for (:"+ type.label() +" { "+ keyName +": \""+ keyVal +"\" })");
            }

            return result;
        } catch (Exception e) {
            throw new CypherException("Cypher.findAll unable to retrieve matches for (:"+ type.label() +" { "+ keyName +": \""+ keyVal +"\" })", e);
        }
    }

    public ArrayList<Node> findAll(NodeType type, String keyName, String keyValue)
    throws CypherException
    {
        try (
                Transaction tx = db.beginTx();
                ResourceIterator<org.neo4j.graphdb.Node> matches = db.findNodes(type.label(), keyName, keyValue)
        ) {
            ArrayList<Node> matchNodes = new ArrayList<>();
            while ( matches.hasNext() )
            {
                matchNodes.add( new Node(matches.next()) );
            }

            tx.success();
            matches.close();
            return matchNodes;
        } catch (Exception e) {
            throw new CypherException("Cypher.findAll unable to retrieve matches for (:"+ type.label() +" { "+ keyName +": \""+ keyValue +"\" })", e);
        }
    }

    public void query (QueryResultCollector query)
    throws CypherException
    {
        String command = query.getCommand();

        try (
                Transaction tx = db.beginTx();
                Result result = db.execute(command)
        ) {
            query.collectResult(result);
            tx.success();
        } catch (Throwable e) {
            throw new CypherException("Cypher.query failed: "+command, e);
        }
    }

    public Node querySingleNode (String command, String resultKey)
    throws Throwable
    {
        Transaction tx = db.beginTx();
        Result result = db.execute(command);

        Map<String, Object> line = result.next();
        if (line.containsKey(resultKey)) {
            Object neoNode = line.get(resultKey);
            if (neoNode instanceof org.neo4j.graphdb.Node) {
                return new Node((org.neo4j.graphdb.Node) neoNode);
            }
        }

        tx.success();
        tx.close();

        return null;
    }

    public void execute (String command)
    throws Throwable
    {
        try (
            Transaction tx = db.beginTx()
        ) {
            db.execute(command);
            tx.success();
        }
    }

    @Override
    public void close()
    {
        // @todo check4 memory-leak: Topic and Resource close should release memory, but if not, will need to manage Node instances here
    }
}
