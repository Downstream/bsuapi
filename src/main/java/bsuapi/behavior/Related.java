package bsuapi.behavior;

import bsuapi.dbal.Topic;
import org.json.JSONObject;
import org.neo4j.graphdb.Node;

/**
 * @todo Behaviors and Resources should be abstracted
 * would allow for more portable behaviors, and reusable resources
 * class Related implements interface Behavior
 * GenericResource path selects Behavior
 */
public class Related
{
    public Topic topic;
    public Node node;
    public String message;

    public Related(Topic topic)
    {
        this.topic = topic;
        this.node = topic.getNode();
        if (topic.hasMatch()) {
            this.message = "Found :"+ topic.name() +" "+ topic.getNodeName() +" and "+ topic.altsCount() +" similar matches.";
        } else {
            this.message = "No Match Found For :"+ topic.name();
        }
    }

    public JSONObject toJson()
    {
        JSONObject data = new JSONObject();
        data.put("topic", topic.name());
        data.put("node", topic.toJson());
        data.put("nearby", topic.altsJson());
        return data;
    }
}
