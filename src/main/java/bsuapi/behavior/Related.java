package bsuapi.behavior;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.NodeType;
import bsuapi.dbal.Topic;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.TopicSharedRelations;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.graphdb.Node;

public class Related extends Behavior
{
    private JSONObject related;

    public Related(Topic topic) {
        super(topic);
    }

    @Override
    public void resolveBehavior(Cypher cypher)
    throws CypherException
    {
        this.related = this.findRelated(cypher);
    }

    public JSONObject toJson()
    {
        JSONObject data = new JSONObject();
        data.put("topic", this.topic.name());
        data.put("node", this.topic.toJson());
        data.put("related", this.related);

        return data;
    }

    protected JSONObject findRelated(Cypher cypher)
    throws CypherException
    {
        JSONObject result = new JSONObject();
        for (NodeType n : NodeType.values()) {
            if (n.isTopic()) {
                result.put(n.labelName(), this.relatedByType(cypher, topic, n));
            }

            result.put(NodeType.ARTWORK.labelName(), this.relatedByType(cypher, topic, NodeType.ARTWORK));
        }

        return result;
    }

    protected JSONArray relatedByType(Cypher cypher, Topic topic, NodeType type)
    throws CypherException
    {
        JSONArray result = new JSONArray();
        CypherQuery q = TopicSharedRelations.params(topic, type);
        for (Node node : cypher.query(q)) {
            result.put(node);
        }
        return result;
    }

    public static BehaviorDescribe describe()
    {
        BehaviorDescribe desc = BehaviorDescribe.resource("/related/{TOPIC}/{VALUE}",
            "Find all (TOPIC)s with an indexed value matching (VALUE), along with a" +
            "collection of closely related Topics, and a collection of Artwork which references that Topic. "
        );

        desc.arg("topic", "All lowercase, a-z. Search all topics: 'topic'");
        desc.arg("value", "Must start with a letter, a-zA-Z_0-9. Use underscores for spaces (Union_Porcelain_Works).");

        JSONArray topics = new JSONArray();
        for (NodeType n : NodeType.values()) {
            if (n.isTopic()) {
                topics.put(n.labelName());
            }
        }
        desc.put("topics", topics);

        return desc;
    }


}
