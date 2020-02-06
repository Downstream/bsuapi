package bsuapi.behavior;

import bsuapi.dbal.*;
import bsuapi.dbal.query.IndexQuery;
import bsuapi.resource.Util;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.logging.Log;

public class IndexQueryBehavior extends Behavior
{
    private JSONArray searchResults = null;
    private long searchResultCount = 0;
    private IndexQuery indexQuery;
    private String resultKey;

    public IndexQueryBehavior(IndexQuery indexQuery) {
        super();
        this.indexQuery = indexQuery;
        this.resultKey = indexQuery.getName();
    }

    public IndexQueryBehavior(IndexQuery indexQuery, String resultKey) {
        super();
        this.indexQuery = indexQuery;
        this.resultKey = resultKey;
    }

    public int length() { return (null != this.searchResults) ? this.searchResults.length() : 0; }

    @Override
    public String getBehaviorKey() { return this.resultKey; }

    @Override
    public Object getBehaviorData() { return this.searchResults; }

    @Override
    public void resolveBehavior(Cypher cypher)
    throws CypherException
    {
        this.searchResults = this.indexQuery.exec(cypher);
        this.searchResultCount = this.indexQuery.getResultCount();
        super.resolveBehavior(cypher);
    }

    @Override
    public String buildMessage(Topic topic)
    {
        // most behaviors should have a topic (may need to refactor to abstract the topic dependency)
        // this case will never have a specific topic, so it will always be null
        if (this.length()>0) {
            return  this.length() +" of "+ this.searchResultCount +" matches for "+ this.indexQuery.toString();
        }

        return "No results found for "+ this.indexQuery.toString();
    }

    private String getFirstItemIndex()
    {
        if (this.indexQuery.page > 1) {
            return ""+ (((this.indexQuery.page -1) * this.indexQuery.limit) + 1);
        }

        return "1";
    }

    @Override
    public JSONObject toJson()
    {
        JSONObject data = new JSONObject();

        if (this.length() > 0) {
            data.put("resultCount", this.searchResultCount);

            JSONObject best = this.searchResults.optJSONObject(0);
            if (!best.isEmpty()) {
                data.put("node", best);

                String labelName = best.optString("type");
                if (!labelName.isEmpty()) {
                    data.put("topic", labelName);
                }
            }
        }

        this.putBehaviorData(data);
        this.putAppendedBehaviorData(data);
        return data;
    }

    @Override
    public void addToLog (Log log)
    {
        log.info(
            "Behavior debuging for "+ this.getClass().getName() +
                "\n    query: " + this.indexQuery.toString() +
                "\n    message: " + this.message
        );
    }

    public static BehaviorDescribe describe()
    {
        BehaviorDescribe desc = BehaviorDescribe.resource("/search/{QUERY}",
                "Search by simplified lucene full-text-index of all Topics, url-encoded."
        );

        desc.arg("query", "url-encoded lucene search. simplest example: greek");

        desc.put("syntax",Util.readResourceJSON("searchDescribe"));

        return desc;
    }
}