package bsuapi.dbal;

import bsuapi.dbal.query.CypherQuery;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

import java.util.ArrayList;

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
            Node result = matches.get(0);
            matches.remove(0);
            return result;
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
                ResourceIterator<Node> matches = db.findNodes(type.label(), keyName, keyValue)
        ) {
            ArrayList<Node> matchNodes = new ArrayList<>();
            while ( matches.hasNext() )
            {
                matchNodes.add( matches.next() );
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
                Result nodes = db.execute(query.getCommand())
        ) {
            ArrayList<Node> matchNodes = new ArrayList<>();
            while ( nodes.hasNext() )
            {
                matchNodes.add((Node) nodes.next());
            }

            tx.success();
            nodes.close();
            return matchNodes;
        } catch (Exception e) {
            throw new CypherException("Cypher.query failed: "+query, e);
        }
    }

//    public ArrayList<Node> topicRelated(Topic topic, NodeType related)
//    {
//        // @todo relatedTopics
//    }


    @Override
    public void close() {

    }
}
