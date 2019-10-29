package bsuapi.dbal.query;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.Node;
import bsuapi.dbal.NodeType;
import bsuapi.resource.Config;
import org.json.JSONArray;
import org.json.JSONObject;

abstract public class CypherQuery {
    protected String initQuery;
    protected String resultQuery;
    private JSONArray results;
    public int limit = 20;
    public int page = 0;
    public NodeType target;
    public static String resultColumn = "t"; // target

    public CypherQuery (String query)
    {
        this.initQuery = query;
        this.resultQuery = query;
    }

    abstract public String getCommand();

    public void setLimit(String limit)
    {
        int lim = this.limit;
        try {
            lim = Integer.parseInt(limit);
            lim = Math.abs(lim);
        } catch (NumberFormatException e) {
            lim = this.limit;
        } finally {
            this.setLimit(lim);
        }
    }

    public void setLimit(int limit)
    {
        if (limit == 0) limit = 1000;
        this.limit = limit;
    }

    public void setPage(String page)
    {
        int p = this.page;
        try {
            p = Integer.parseInt(page);
            p = Math.abs(p);
        } catch (NumberFormatException e) {
            p = this.page;
        } finally {
            this.setPage(p);
        }
    }

    public void setPage(int page)
    {
        this.page = page;
    }

    public String getPageLimitCmd()
    {
        String result = "";
        if (this.page > 1) {
            result += " SKIP " + ((this.page -1) * this.limit);
        }

        return result + " LIMIT " + this.limit + " ";
    }

    public String toString()
    {
        return this.getClass().getSimpleName() +" "+ this.initQuery;
    }

    public Node entryHandler(org.neo4j.graphdb.Node neoNode)
    {
        return new Node(neoNode);
    }

    public String entryHandler(String entry) {
        return entry;
    }

    public String entryHandler(Object entry) {
        return entry.toString();
    }

    public void addResultEntry(Node entry)
    {
        if (this.results == null) {
            this.results = new JSONArray();
        }

        JSONObject n = entry.toJsonObject();
        if (this.target != null) {
            if (this.target.isTopic()) {
                String topicPath = entry.getUri(this.target);
                n.put("linkRelated", Config.buildUri("/related" + topicPath));
                n.put("linkAssets", Config.buildUri("/topic-assets" + topicPath));
            }
        }

        this.results.put(n);
    }

    public void addResultEntry(String entry)
    {
        if (this.results == null) {
            this.results = new JSONArray();
        }

        this.results.put(entry);
    }

    public JSONArray exec(Cypher c)
    throws CypherException
    {
        c.query(this);
        return this.results;
    }
}
