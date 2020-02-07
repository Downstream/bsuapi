package bsuapi.behavior;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.Topic;
import bsuapi.dbal.query.CypherQuery;
import org.json.JSONObject;
import org.neo4j.logging.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Behavior
{
    protected String message;
    protected ArrayList<Behavior> appendedBehaviors;
    protected Map<String, String> config;
    protected CypherQuery query;

    protected Behavior(Map<String, String> config)
    {
        this.setConfig(config);
    }

    public void resolveBehavior(Cypher cypher)
    throws CypherException
    {
        if (null != this.appendedBehaviors) {
            for (Behavior child : this.appendedBehaviors) {
                child.setConfig(this.config);
                child.resolveBehavior(cypher);
            }
        }

        this.message = this.buildMessage();
    }

    abstract public String getBehaviorKey();
    abstract public Object getBehaviorData();
    abstract public String buildMessage();

    public static Map<String, String> defaultConfig()
    {
        Map<String, String> m = new HashMap<>();
        m.put("limit", "20");
        return m;
    }

    public Map<String, String> cleanConfig(Map<String, String> map)
    {
        Map<String, String> result = (null == this.config)
            ? Behavior.defaultConfig()
            : this.config
            ;

        if (null != map) {
            for (String key : new String[]{"limit","page", Topic.keyParam, Topic.labelParam, Search.searchParam}) {
                if (map.containsKey(key)) {
                    result.put(key, map.get(key));
                }
            }
        }

        return result;
    }

    public void setConfig(Map<String, String> config)
    {
        this.config = this.cleanConfig(config);
    }

    public String getConfigParam(String key)
    {
        return this.config.get(key);
    }

    public void setQueryConfig(CypherQuery query)
    {
        query.setLimit(this.getConfigParam("limit"));
        query.setPage(this.getConfigParam("page"));
    }

    public void putBehaviorData(JSONObject json)
    {
        json.put(this.getBehaviorKey(), this.getBehaviorData());
    }

    public void putAppendedBehaviorData(JSONObject json)
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

    public String getMessage()
    {
        if (null == this.message) {
            return this.getClass().getSimpleName() + " not Resolved.";
        } else {
            return this.message;
        }
    }

    public JSONObject toJson()
    {
        JSONObject data = new JSONObject();
        this.putBehaviorData(data);
        this.putAppendedBehaviorData(data);
        return data;
    }

    public void addToLog (Log log)
    {
        log.debug(
            "Behavior debuging for "+ this.getClass().getName() +
            "\n    message: " + this.message
        );
    }
}
