package bsuapi.behavior;

import bsuapi.dbal.*;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.TopicSharedRelations;
import org.json.JSONObject;

import java.util.Map;

public class Related extends Behavior
{
    private JSONObject related;
    public Topic topic;
    public Node node;

    public Related(Map<String, String> config)
    throws BehaviorException
    {
        super(config);

        String labelName = this.getConfigParam(Topic.labelParam);
        String keyName = this.getConfigParam(Topic.keyParam);

        if (null == labelName || null == keyName) {
            throw new BehaviorException("Missing required parameters for "+ this.toString()+ ": "+ Topic.labelParam +" and "+ Topic.keyParam);
        }

        this.topic = new Topic(NodeType.match(labelName), keyName);
    }

    public org.neo4j.graphdb.Node getNeoNode() throws CypherException { return this.node.getNeoNode(); }

    @Override
    public String getBehaviorKey() { return "related"; }

    @Override
    public JSONObject getBehaviorData() { return this.related; }

    @Override
    public String buildMessage()
    {
        if (this.topic == null) {
            return "No Match Found";
        } else if (this.topic.hasMatch()) {
            return "Found :"+ this.topic.name() +" {"+ this.topic.getNodeKeyField() +":'"+ this.topic.getNodeKey() +"'}";
        } else {
            return "No Match Found For :"+ this.topic.name();
        }
    }

    private void resolveTopic(Cypher cypher)
    throws CypherException
    {
        if (!this.topic.hasMatch()) { cypher.resolveNode(this.topic); }
        this.node = topic.getNode();
    }

    @Override
    public void resolveBehavior(Cypher cypher)
    throws CypherException
    {
        this.resolveTopic(cypher);

        this.related = new JSONObject();
        for (NodeType n : NodeType.values()) {
            if (n.isTopic()) {
                CypherQuery query = new TopicSharedRelations(this.topic, n);
                this.setQueryConfig(query);
                this.related.put(n.friendlyName(), query.exec(cypher));
            }
        }

        super.resolveBehavior(cypher);
    }

    @Override
    public JSONObject toJson()
    {
        JSONObject data = super.toJson();
        data.put("topic", this.topic.name());
        data.put("node", this.topic.toJson());
        return data;
    }

    public static BehaviorDescribe describe()
    {
        BehaviorDescribe desc = BehaviorDescribe.resource("/related/{type}/{GUID}",
            "Retrieve a specific node by GUID, along with a" +
            "collection of closely related Topics, and all directly related Assets. "
        );

        desc.arg("type", "All lowercase, a-z. May be a topic-type (artist, nation, culture, etc.), 'folder', or 'asset'.");
        desc.arg("GUID", "URL-encoded string. Must start with a letter, a-zA-Z.");

        return desc;
    }
}
