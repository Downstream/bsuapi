package bsuapi.dbal;

import bsuapi.dbal.query.CypherQuery;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
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

    public void resolveTopic(Topic topic)
    throws CypherException
    {
        topic.setNode(this.findOne(topic.getType(), topic.getNodeKeyField(), topic.getNodeKey()));
    }

    public Node findOne(NodeType type, String keyName, String keyVal)
    throws CypherException
    {
        ArrayList<Node> matches = new ArrayList<>(this.findAll(type, keyName, keyVal));

        // @todo pick by (curator/algo) score
        if (matches.size() > 0)
        {
            return matches.get(0);
            // Node result = matches.get(0);
            // matches.remove(0);
            // return result;
        }

        return null;
    }

    public ArrayList<Node> bestMatchThroughIndices (NodeType type, String keyVal)
    throws CypherException
    {
        Schema schema = db.schema();
        ArrayList<Node> matches = new ArrayList<>();

        for (IndexDefinition index : schema.getIndexes(type.label()))
        {
            for (String keyName : index.getPropertyKeys())
            {
                matches.addAll(this.findAll(type, keyName, keyVal));
            }
        }

        return matches;
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

    public ArrayList<Node> query (CypherQuery query)
    throws CypherException
    {
        try (
                Transaction tx = db.beginTx();
                Result result = db.execute(query.getCommand());
        ) {

            ArrayList<Node> matchNodes = new ArrayList<>();

            Iterator<org.neo4j.graphdb.Node> targetResults = result.columnAs(CypherQuery.resultColumn);
            for (org.neo4j.graphdb.Node neoNode : Iterators.asIterable(targetResults)) {
                matchNodes.add(new Node(neoNode));
            }

            tx.success();
            result.close();
            return matchNodes;
        } catch (Exception e) {
            throw new CypherException("Cypher.query failed: "+query, e);
        }
    }

    public Result rawQuery (CypherQuery query)
    throws CypherException
    {
        try (
                Transaction tx = db.beginTx();
        ) {
//            Result r = db.execute(query.getCommand());
//            StringBuilder rows = new StringBuilder();
//            while ( r.hasNext()) {
//                Map<String,Object> row = r.next();
//                for ( Map.Entry<String,Object> column : row.entrySet() )
//                {
//                    rows.append(column.getKey()).append(": ").append(column.getValue()).append("; ");
//                }
//                rows.append("\n");
//            }
//            result.put("result", rows);
            return db.execute(query.getCommand());
        } catch (Exception e) {
            throw new CypherException("Cypher.query failed: "+query, e);
        }
    }

    @Override
    public void close() {
        // @todo check4 memory-leak: Topic and Resource close should release memory, but if not, will need to manage Node instances here
    }
}
