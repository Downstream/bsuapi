package bsuapi.behavior;

import bsuapi.dbal.*;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.TopicAssets;
import org.json.JSONArray;

public class Assets extends Behavior
{
    private JSONArray assets;

    public Assets(Topic topic) {
        super(topic);
    }

    @Override
    public String getBehaviorKey() { return "assets"; }

    @Override
    public Object getBehaviorData() { return this.assets; }

    @Override
    public void resolveBehavior(Cypher cypher)
    throws CypherException
    {
        CypherQuery query = new TopicAssets(topic);
        query.setLimit(this.getConfigParam("limit"));
        this.assets = query.exec(cypher);
        super.resolveBehavior(cypher);
    }

    public static BehaviorDescribe describe()
    {
        BehaviorDescribe desc = BehaviorDescribe.resource("/assets/{TOPIC}/{VALUE}",
            "Find the top scored assets related to a TOPIC who's key matches VALUE "
        );

        desc.arg("topic", "All lowercase, a-z. Search all topics: 'topic'");
        desc.arg("value", "URL-encoded string. Must start with a letter, a-zA-Z.");

        return desc;
    }


}
