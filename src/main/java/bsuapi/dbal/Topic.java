package bsuapi.dbal;

import org.json.JSONObject;

public class Topic implements org.neo4j.graphdb.Label
{
    private NodeType type;
    private String nodeKey;
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

        JSONObject n = node.toJsonObject();

        if (this.type.isTopic()) {
            String uri = node.getUri(this.type);
            if (null != uri) {
                n.put("linkRelated", uri);
            }
        }

        return n;
    }

    public String toCypherMatch()
    {
        return String.format(":%1$s {%2$s:\"%3$s\"}", this.name(), this.getNodeKeyField(), this.getNodeKey());
    }
}
