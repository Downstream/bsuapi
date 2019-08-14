package bsuapi.dbal;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

import java.util.ArrayList;

public class Topic implements org.neo4j.graphdb.Label {

    private GraphDatabaseService db;

    private org.neo4j.graphdb.Label label;

    public Topic(GraphDatabaseService db, String labelName)
    {
        this.db = db;
        this.label = org.neo4j.graphdb.Label.label(labelName);
    }

    @Override
    public String name() {
        return this.label.name();
    }

    public ArrayList<Node> findRelated (
            String value
    ){
        Schema schema = db.schema();
        ArrayList<Node> matches = new ArrayList<>();

        for (IndexDefinition index : schema.getIndexes(label)) {
            for (String keyName : index.getPropertyKeys()) {
                matches.addAll(this.findThroughIndex(keyName, value));
            }
        }

        return matches;
    }

    private ArrayList<Node> findThroughIndex(
            String keyName,
            String keyValue
    ){
        try ( ResourceIterator<Node> matches = db.findNodes(label, keyName, keyValue) )
        {
            ArrayList<Node> matchNodes = new ArrayList<>();
            while ( matches.hasNext() )
            {
                matchNodes.add( matches.next() );
            }

            matches.close();
            return matchNodes;
        }
    }
}
