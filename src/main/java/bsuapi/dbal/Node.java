package bsuapi.dbal;

import bsuapi.resource.URLCoder;
import org.json.JSONObject;

import java.util.Map;
import java.lang.String;
import java.lang.Object;

public class Node
{
    private org.neo4j.graphdb.Node neoNode;
    private Map<String, Object> properties;
    private String keyName = "name";
    private String keyVal;

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

    public org.neo4j.graphdb.Node getNeoNode() { return this.neoNode; }

    public String getNodeKeyField() { return this.keyName; }

    public String getNodeKey() { return this.keyVal; }

    public String getProperty(String key){ return (String) this.properties.get(key); }

    public String getUri(NodeType type)
    {
        if (!type.isTopic()) {
            return null;
        }

        return "/related/" + type.labelName().toLowerCase() + "/" + URLCoder.encode(this.getNodeKey());
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

        return result;
    }
}
