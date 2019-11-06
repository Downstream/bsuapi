package bsuapi.dbal;

import bsuapi.resource.Config;
import org.json.JSONObject;

public class Topic implements org.neo4j.graphdb.Label
{
    protected NodeType type;
    protected String nodeKey;
    private Node node;

    public Topic(String labelName, String nodeKey)
    {
        this.type = NodeType.match(labelName);
        this.nodeKey = nodeKey;
    }

    public void setNode(Node node)
    {
        this.node = node;
    }

    @Override
    public String name() {
        return this.type.label().name();
    }

    public String getNodeKeyField() { return "name"; }

    public String getNodeKey() { return this.nodeKey; }

    public Boolean hasMatch() { return (node != null); }

    public Node getNode() { return this.node; }

    public NodeType getType() { return this.type; }

    public String getNodeProperty(String field)
    {
        if (this.hasMatch())
        {
            return this.getNode().getProperty(field);
        }

        return "";
    }

    public JSONObject toJson()
    {
        Node node = this.getNode();
        if (null == node) {
            return null;
        }

        if (null == node.type) {
            node.type = this.type;
        }

        return node.toJsonObject();
    }

    public String toCypherMatch()
    {
        return String.format(":%1$s {%2$s:\"%3$s\"}", this.name(), this.getNodeKeyField(), this.getNodeKey());
    }
}
