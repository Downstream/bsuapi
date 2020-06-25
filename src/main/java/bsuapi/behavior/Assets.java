package bsuapi.behavior;

import bsuapi.dbal.*;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.TopicAssets;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class Assets extends Behavior
{
    private JSONArray assets;
    public Topic topic;
    public Node node;
    public String query;

    public Assets(Map<String, String> config)
    throws BehaviorException
    {
        super(config);
        String labelName = this.getConfigParam(Topic.labelParam);
        String keyName = this.getConfigParam(Topic.keyParam);

        if (null == labelName || null == keyName) {
            throw new BehaviorException("Missing required parameters for "+ this.toString()+ ": "+ Topic.labelParam +" and "+ Topic.keyParam);
        }

        this.topic = new Topic(labelName, keyName);
    }

    @Override
    public String getBehaviorKey() { return "assets"; }

    @Override
    public JSONArray getBehaviorData() { return this.assets; }

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

        CypherQuery query = new TopicAssets(this.topic);
        this.setQueryConfig(query);
        this.assets = query.exec(cypher);
        this.query = query.getCommand();
        super.resolveBehavior(cypher);
    }

    @Override
    public JSONObject toJson()
    {
        JSONObject data = super.toJson();
        data.put("node", this.topic.toJson());
        data.put("query", this.query);
        return data;
    }

    public static BehaviorDescribe describe()
    {
        BehaviorDescribe desc = BehaviorDescribe.resource("/topic-assets/{TOPIC}/{VALUE}",
            "Find the top scored assets related to a TOPIC who's key matches VALUE "
        );

        desc.arg("topic", "All lowercase, a-z. Search all topics: 'topic'");
        desc.arg("value", "URL-encoded string. Must start with a letter, a-zA-Z.");

        return desc;
    }
}
