package bsuapi.behavior;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.Topic;
import bsuapi.dbal.Node;
import org.json.JSONObject;
import org.neo4j.logging.Log;

import java.util.ArrayList;

public abstract class Behavior {
    public Topic topic;
    public Node node;
    protected String message;
    protected ArrayList<Behavior> appendedBehaviors;

    public Behavior(Topic topic)
    {
        this.topic = topic;
        this.node = topic.getNode();
    }

    public void resolveBehavior(Cypher cypher)
    throws CypherException
    {
        this.message = this.buildMessage(this.topic);
    }

    abstract public String getBehaviorKey();
    abstract public Object getBehaviorData();

    public void putBehaviorData(JSONObject json)
    {
        json.put(this.getBehaviorKey(), this.getBehaviorData());
    }

    public void putAppendedBehaviors(JSONObject json)
    {
        if (null == this.appendedBehaviors) {
            return;
        }

        for (Behavior child : this.appendedBehaviors) {
            child.putBehaviorData(json);
        }
    }

    public void appendBehavior(Behavior child)
    {
        if (null == this.appendedBehaviors) {
            this.appendedBehaviors = new ArrayList<>();
        }
        this.appendedBehaviors.add(child);
    }

    public org.neo4j.graphdb.Node getNeoNode() { return this.node.getNeoNode(); }

    public String getMessage() {
        if (null == this.message) {
            return this.getClass().getSimpleName() + " not Resolved.";
        } else {
            return this.message;
        }
    }

    public String buildMessage(Topic topic)
    {
        if (topic.hasMatch()) {
            return "Found :"+ topic.name() +" {"+ topic.getNodeKeyField() +":\""+ topic.getNodeKey() +"\"}";
        } else {
            return "No Match Found For :"+ topic.name();
        }
    }

    public JSONObject toJson()
    {
        JSONObject data = new JSONObject();
        data.put("topic", this.topic.name());
        data.put("node", this.topic.toJson());
        this.putBehaviorData(data);
        this.putAppendedBehaviors(data);
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
