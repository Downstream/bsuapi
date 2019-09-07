package bsuapi.behavior;

import bsuapi.dbal.Topic;
import org.json.JSONObject;
import org.neo4j.graphdb.Node;
import org.neo4j.logging.Log;

public abstract class Behavior {
    public Topic topic;
    public Node node;
    public String message;

    public Behavior(Topic topic)
    {
        this.topic = topic;
        this.node = topic.getNode();
        this.message = this.buildMessage(topic);
    }

    public String buildMessage(Topic topic)
    {
        if (topic.hasMatch()) {
            return "Found :"+ topic.name() +" "+ topic.getNodeName() +" and "+ topic.altsCount() +" similar matches.";
        } else {
            return "No Match Found For :"+ topic.name();
        }
    }

    public JSONObject toJson()
    {
        JSONObject data = new JSONObject();
        data.put("topic", this.topic.name());
        data.put("node", this.topic.toJson());
        data.put("nearby", this.topic.altsJson());
        return data;
    }

    public void debug (Log log)
    {
        log.info(
            "Behavior debuging for "+ this.getClass().getName() +
            "\n    topic: " + this.topic.toString() +
            "\n    node: " + this.node.toString() +
            "\n    message: " + this.message
        );
    }
}
