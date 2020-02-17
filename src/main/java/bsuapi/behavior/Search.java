package bsuapi.behavior;

import bsuapi.resource.Util;
import org.json.JSONObject;
import org.neo4j.logging.Log;

import java.util.Map;

public class Search extends Behavior
{
    public static final String searchParam = "search";

    private String query;

    public Search(Map<String, String> config)
    throws BehaviorException
    {
        super(config);

        this.query = this.getConfigParam(Search.searchParam);
        if (null == this.query) {
            throw new BehaviorException("Missing required parameter for IndexBehavior "+ getClass() +": "+ Search.searchParam);
        }
    }

    @Override
    public String getBehaviorKey() { return "search"; }

    @Override
    public Object getBehaviorData()
    {
        JSONObject data = new JSONObject();
        data.put("title", "Search for "+ this.query);
        data.put("description", "Assets and Topics ordered by the best matches to the query provided.");
        return data;
    }

    @Override
    public String buildMessage()
    {
        StringBuilder result = new StringBuilder();
        result.append("Results for topics and assets: ");

        for (Behavior child : this.appendedBehaviors) {
            result.append(child.buildMessage()).append(", ");
        }

        return result.toString();
    }

    @Override
    public JSONObject toJson()
    {
        JSONObject data = new JSONObject();
        this.putBehaviorData(data);
        this.putAppendedBehaviorData(data);
        return data;
    }

    @Override
    public void addToLog (Log log)
    {
        for (Behavior child : this.appendedBehaviors) {
            child.addToLog(log);
        }
    }

    public static BehaviorDescribe describe()
    {
        BehaviorDescribe desc = BehaviorDescribe.resource("/search/{QUERY}",
                "Search by simplified lucene full-text-index of all Topics and Assets, url-encoded."
        );

        desc.arg("query", "url-encoded lucene search. simplest example: greek");
        desc.put("syntax",Util.readResourceJSON("searchDescribe"));

        return desc;
    }

    public static BehaviorDescribe describeCompletion()
    {
        BehaviorDescribe desc = BehaviorDescribe.resource("/search_completion/{QUERY}",
                "Return a list of strings of most like Topic matches for QUERY."
        );

        desc.arg("query", "url-encoded lucene search. simplest example: greek");
        desc.put("syntax", "see /search/ syntax");

        return desc;
    }
}
