package bsuapi.behavior;

import bsuapi.dbal.*;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.TopicSharedRelations;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class Related extends Behavior
{
    private JSONObject related;
    public Topic topic;
    public Node node;

    public Related(Map<String, String> config) {
        super(config);
        this.topic = new Topic(this.getConfigParam(Topic.labelParam), this.getConfigParam(Topic.keyParam));
    }

    public org.neo4j.graphdb.Node getNeoNode() { return this.node.getNeoNode(); }

    @Override
    public String getBehaviorKey() { return "related"; }

    @Override
    public Object getBehaviorData() { return this.related; }

    @Override
    public String buildMessage()
    {
        if (this.topic == null) {
            return "No Match Found";
        } else if (this.topic.hasMatch()) {
            return "Found :"+ this.topic.name() +" {"+ this.topic.getNodeKeyField() +":\""+ this.topic.getNodeKey() +"\"}";
        } else {
            return "No Match Found For :"+ this.topic.name();
        }
    }

    private void resolveTopic(Cypher cypher)
    throws CypherException
    {
        if (!this.topic.hasMatch()) { cypher.resolveTopic(this.topic); }
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
                this.related.put(n.labelName(), query.exec(cypher));
            }
        }

        super.resolveBehavior(cypher);
    }

    @Override
    public JSONObject toJson() {
        JSONObject data = super.toJson();
        data.put("topic", this.topic.name());
        data.put("node", this.topic.toJson());
        return data;
    }

    public static BehaviorDescribe describe()
    {
        BehaviorDescribe desc = BehaviorDescribe.resource("/related/{TOPIC}/{VALUE}",
            "Find all (TOPIC)s with an indexed value matching (VALUE), along with a" +
            "collection of closely related Topics, and a collection of Artwork which references that Topic. "
        );

        desc.arg("topic", "All lowercase, a-z. Search all topics: 'topic'");
        desc.arg("value", "URL-encoded string. Must start with a letter, a-zA-Z.");

        return desc;
    }


}
