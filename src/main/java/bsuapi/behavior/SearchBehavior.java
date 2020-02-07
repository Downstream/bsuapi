package bsuapi.behavior;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.query.IndexQuery;
import bsuapi.resource.URLCoder;
import bsuapi.resource.Util;
import org.json.JSONObject;
import org.neo4j.logging.Log;

public class SearchBehavior extends Behavior
{
    private String query;
    private IndexQuery topicQuery;
    private IndexQuery assetQuery;

    public SearchBehavior(String query) {
        this.query = query;

        this.topicQuery = new IndexQuery("topicNameIndex", URLCoder.decode(query));
        this.assetQuery = new IndexQuery("assetNameIndex", URLCoder.decode(query));
        this.appendBehavior(new IndexQueryBehavior(this.topicQuery, "topics"));
        this.appendBehavior(new IndexQueryBehavior(this.assetQuery, "assets"));
    }

    public int length() {
        int result = 0;
        for (Behavior child : this.appendedBehaviors) {
            if (child instanceof IndexQueryBehavior) {
                result += ((IndexQueryBehavior) child).length();
            }
        }

        return result;
    }

    @Override
    public String getBehaviorKey() { return "search-results"; }

    @Override
    public Object getBehaviorData() {
        JSONObject data = new JSONObject();
        data.put("title", "Search for "+ this.query);
        data.put("description", "Assets and Topics ordered by the best matches to the query provided.");
        return data;
    }

    @Override
    public void resolveBehavior(Cypher cypher) throws CypherException {
        this.setQueryConfig(this.topicQuery); // @todo: needs refactor of Behavior/BehaviorType/Topic/CypherQuery dependency pattern
        this.setQueryConfig(this.assetQuery);

        super.resolveBehavior(cypher);
    }

    @Override
    public String buildMessage()
    {
        StringBuilder result = new StringBuilder();
        result.append("Results for topics and assets: ");

        for (Behavior child : this.appendedBehaviors) {
            result.append(child.buildMessage()).append(", ");
        }

        return result
            .append("with ")
            .append(this.topicQuery.getResultCount() + this.assetQuery.getResultCount())
            .append(" total matches.")
            .toString();
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
}
