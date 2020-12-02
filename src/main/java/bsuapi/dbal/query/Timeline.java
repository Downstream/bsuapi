package bsuapi.dbal.query;

import bsuapi.dbal.*;
import bsuapi.resource.JsonResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class Timeline extends CypherQuery
implements QueryResultSingleColumn
{
    protected static String query =
        "MATCH (%1$s)<-[r:%2$s]-("+ QueryResultSingleColumn.resultColumn +":%3$s) " +
        "%4$s RETURN "+ QueryResultSingleColumn.resultColumn + " " +
        "ORDER BY "+ QueryResultSingleColumn.resultColumn +".date ASC"
        ;

    protected static String[] clauses = new String[]{
        "EXISTS(" + QueryResultSingleColumn.resultColumn +".date)"
    };

    protected JSONObject results;

    protected Topic topic;
    protected TimeStep step;

    public Timeline(Topic topic)
    throws NullPointerException
    {
        super(Timeline.query);
        this.target = NodeType.ASSET;
        this.topic = topic;
        this.step = TimeStep.stepFromTopic(topic);
    }

    public JSONObject getResults()
    {
        this.results.put("step",this.step);
        return this.results;
    }

    public String getCommand()
    {
        return this.resultQuery = String.format(
            this.initQuery,
            this.topic.toCypherMatch(),
            this.topic.getType().relFromAsset(),
            this.target.labelName(),
            this.where(Timeline.clauses)
        ) + this.getPageLimitCmd();
    }

    public void entryHandler(Object entry)
    {
        try {
            Node asset;
            if (entry instanceof org.neo4j.graphdb.Node) {
                asset = new Node((org.neo4j.graphdb.Node) entry);
            } else if (entry instanceof Map) {
                asset = new VirtualNode((Map) entry);
            } else {
                this.addEntry("error", entry.getClass() + ": " + entry.toString());
                return;
            }

            this.addEntry(this.step.getDateKey(asset), asset.toJsonObject());

        } catch (Throwable e) {
            this.results.put("exeption", JsonResponse.exceptionDetailed(e));
        }
    }

    protected void addEntry(String dateGroup, Object entry)
    {
        if (this.results == null) {
            this.results = new JSONObject();
        }

        JSONArray groupResults = this.results.optJSONArray(dateGroup);

        if (groupResults == null) {
            groupResults = new JSONArray();
        }

        groupResults.put(entry);

        this.results.put(dateGroup, groupResults);
    }
}
