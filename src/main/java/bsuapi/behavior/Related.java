package bsuapi.behavior;

import bsuapi.dbal.Topic;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.graphdb.Node;

public class Related extends Behavior
{
    public Related(Topic topic) {super(topic);}

    public JSONObject toJson()
    {
        JSONObject data = new JSONObject();
        data.put("topic", this.topic.name());
        data.put("node", this.topic.toJson());
        data.put("nearby", this.topic.altsJson());
        return data;
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
        topics.put(Topic.ARTWORK);
        topics.put(Topic.ARTIST);
        topics.put(Topic.CLASS);
        topics.put(Topic.CULTURE);
        topics.put(Topic.NATION);
        topics.put(Topic.TAG);
        desc.put("topics", topics);

        return desc;
    }


}
