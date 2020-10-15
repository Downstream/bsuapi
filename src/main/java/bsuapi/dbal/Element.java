package bsuapi.dbal;

import org.json.JSONObject;

public class Element implements org.neo4j.graphdb.Label
{
    protected NodeType type;
    protected String nodeKey;
    protected Node node;

    public Element(NodeType type, String nodeKey)
    {
        this.type = type;
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

    public String getNodeKeyField() { return "guid"; }

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
