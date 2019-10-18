package bsuapi.behavior;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.Topic;
import bsuapi.dbal.Node;
import org.json.JSONObject;
import org.neo4j.logging.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Behavior {
    public Topic topic;
    public Node node;
    protected String message;
    protected ArrayList<Behavior> appendedBehaviors;
    protected Map<String, String> config;

    public Behavior(Topic topic)
    {
        this.topic = topic;
        this.node = topic.getNode();
        this.setConfig(Behavior.defaultConfig());
    }

    public void resolveBehavior(Cypher cypher)
    throws CypherException
    {
        if (null != this.appendedBehaviors) {
            for (Behavior child : this.appendedBehaviors) {
                child.resolveBehavior(cypher);
            };
        }

        this.message = this.buildMessage(this.topic);
    }

    abstract public String getBehaviorKey();
    abstract public Object getBehaviorData();

    public static Map<String, String> defaultConfig()
    {
        Map<String, String> m = new HashMap<>();
        m.put("limit", "10");
        return m;
    }

    public static Map<String, String> cleanConfig(Map<String, String> map)
    {
        if (null != map) {
            Map<String, String> result = new HashMap<>();
            for (String key : new String[]{"limit"}) {
                if (map.containsKey(key)) {
                    result.put(key, map.get(key));
                }
            }
        }

        return Behavior.defaultConfig();
    }

    public void setConfig(Map<String, String> config)
    {
        this.config = config;
    }

    public String getConfigParam(String key)
    {
        return this.config.get(key);
    }

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
