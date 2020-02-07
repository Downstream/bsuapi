package bsuapi.behavior;

import bsuapi.dbal.*;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.TopicSharedRelations;
import org.json.JSONArray;
import org.json.JSONObject;

public class Related extends Behavior
{
    private JSONObject related;
    public Topic topic;
    public Node node;

    public Related(Topic topic) {
        super();
        this.topic = topic;
        this.node = topic.getNode();
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

    @Override
    public void resolveBehavior(Cypher cypher)
    throws CypherException
    {
        this.related = new JSONObject();
        JSONArray tmp = new JSONArray();
        for (NodeType n : NodeType.values()) {
            if (n.isTopic()) {
                CypherQuery query = new TopicSharedRelations(this.topic, n);
                this.setQueryConfig(query);
                //tmp.put(query.getCommand());
                this.related.put(n.labelName(), query.exec(cypher));
            }
        }
        //this.related.put("cypher",tmp);
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
