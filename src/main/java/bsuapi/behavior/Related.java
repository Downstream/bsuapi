package bsuapi.behavior;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.NodeType;
import bsuapi.dbal.Topic;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.TopicSharedRelations;
import org.json.JSONArray;
import org.json.JSONObject;

public class Related extends Behavior
{
    private JSONObject related;

    public Related(Topic topic) { super(topic); }

    @Override
    public String getBehaviorKey() { return "related"; }

    @Override
    public Object getBehaviorData() { return this.related; }

    @Override
    public void resolveBehavior(Cypher cypher)
    throws CypherException
    {
        this.related = new JSONObject();
        JSONArray tmp = new JSONArray();
        for (NodeType n : NodeType.values()) {
            if (n.isTopic()) {
                CypherQuery query = new TopicSharedRelations(topic, n);
                this.setQueryConfig(query);
                //tmp.put(query.getCommand());
                this.related.put(n.labelName(), query.exec(cypher));
            }
        }
        //this.related.put("cypher",tmp);
        super.resolveBehavior(cypher);
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
