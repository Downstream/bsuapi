package bsuapi.dbal;

import bsuapi.resource.Config;
import bsuapi.resource.URLCoder;
import org.json.JSONObject;

import java.util.Map;
import java.lang.String;
import java.lang.Object;

public class Node
{
    private org.neo4j.graphdb.Node neoNode;
    protected Map<String, Object> properties;
    protected String keyName = "guid";
    protected String keyVal;
    public NodeType type;

    public Node (org.neo4j.graphdb.Node neoNode)
    {
        this.neoNode = neoNode;
        this.properties = neoNode.getAllProperties();
        this.keyVal = this.getProperty(this.keyName);
    }

    public Node (org.neo4j.graphdb.Node neoNode, String keyName, String keyVal)
    {
        this.neoNode = neoNode;
        this.properties = neoNode.getAllProperties();
        this.keyName = keyName;
        this.keyVal = keyVal;
    }

    protected Node() {}

    public org.neo4j.graphdb.Node getNeoNode() throws CypherException { return this.neoNode; }

    public String getNodeKeyField() { return this.keyName; }

    public String getNodeKey() { return this.keyVal; }

    public String getProperty(String key){ return (String) this.properties.getOrDefault(key, "unknown"); }

    /* @todo refactor this and NodeType calls - can be simplified */
    public String getUri()
    {
        if (null == this.type) {
            return null;
        }

        return "/" + type.labelName().toLowerCase() + "/" + URLCoder.encode(this.getNodeKey());
    }

    public NodeType getType()
    {
        if (null != this.type) {
            return this.type;
        }

        return this.type = NodeType.fromNeoNode(this.neoNode);
    }

    public JSONObject toJsonObject()
    {
        JSONObject result = new JSONObject();
        for (Map.Entry<String, Object> entry : this.properties.entrySet())
        {
            result.put(entry.getKey(), entry.getValue());
        }

        result.put("keyField", this.getNodeKeyField());
        result.put("keyRaw", this.getNodeKey());
        result.put("keyEncoded", URLCoder.encode(this.getNodeKey()));

        NodeType type = this.getType();
        if (null != type) {
            result.put("type", type.labelName());

            /* @todo refactor this and NodeType calls - can be simplified */
            if (type.isTopic()) {
                String topicPath = this.getUri();
                result.put("linkRelated", Config.buildUri("/related" + topicPath));
                result.put("linkAssets", Config.buildUri("/topic-assets" + topicPath));
            } else if (type == NodeType.FOLDER) {
                result.put("linkRelated", Config.buildUri(this.getUri()));
                result.put("linkAssets", Config.buildUri(this.getUri()));
            }
        } else {
            result.put("type", "unknown");
        }

        return result;
    }
}
