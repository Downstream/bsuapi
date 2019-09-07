package bsuapi.dbal;

import bsuapi.resource.URLCoder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

import java.util.ArrayList;

public class Topic implements org.neo4j.graphdb.Label
{
    public static String ARTWORK = "Artwork";
    public static String ARTIST = "Artist";
    public static String CLASS = "Classification";
    public static String CULTURE = "Culture";
    public static String NATION = "Nation";
    public static String TAG = "Tag";

    private GraphDatabaseService db;
    private org.neo4j.graphdb.Label label;
    private Node node;
    private ArrayList<Node> alternates;

    public Topic(GraphDatabaseService db, String labelName, String nodeName)
    {
        this.db = db;
        this.label = org.neo4j.graphdb.Label.label(labelName);
        this.alternates = new ArrayList<>();
        this.node = this.findNode(nodeName);
    }

    @Override
    public String name() {
        return this.label.name();
    }

    private Node findNode (String nodeName)
    {
        Schema schema = db.schema();

        for (IndexDefinition index : schema.getIndexes(label))
        {
            for (String keyName : index.getPropertyKeys())
            {
                alternates.addAll(this.findThroughIndex(keyName, nodeName));
            }
        }

        if (alternates.size() > 0)
        {
            Node result = alternates.get(0);
            alternates.remove(0);
            return result;
        }

        return null;
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

    public String getNodeName()
    {
        if (this.hasMatch())
        {
            Object prop = this.getNode().getProperty("name", null);
            if (prop != null)
            {
                return prop.toString();
            }
        }

        return "";
    }

    public Boolean hasMatch()
    {
        return (node != null);
    }

    public Node getNode()
    {
        return this.node;
    }

    public ArrayList<Node> getAlternates()
    {
        return alternates;
    }

    public int altsCount()
    {
        return alternates.size();
    }

    public JSONObject toJson()
    {
        JSONObject node = NodeUtil.toJsonObject(this.node);
        node.put("nameEncoded", URLCoder.encode(this.getNodeName()));
        return node;
    }

    public JSONArray altsJson()
    {
        JSONArray result = new JSONArray();
        for (Node node : this.alternates)
        {
            result.put(NodeUtil.toJsonObject(node));
        }

        return result;
    }
}
