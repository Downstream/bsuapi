package bsuapi.dbal.query;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.Node;
import bsuapi.dbal.NodeType;
import org.json.JSONArray;
import org.json.JSONObject;

abstract public class CypherQuery {
    protected String[] args;
    protected String initQuery;
    protected String resultQuery;
    private JSONArray results;
    public int limit = 10;
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
        this.limit = limit;
    }

    public String toString()
    {
        return this.getClass().getSimpleName() +" "+ this.initQuery;
    }

    public Node neoEntryHandler(org.neo4j.graphdb.Node neoNode)
    {
        return new Node(neoNode);
    }

    public void addResultEntry(Node entry)
    {
        if (this.results == null) {
            this.results = new JSONArray();
        }

        JSONObject n = entry.toJsonObject();
        if (this.target != null) {
            if (this.target.isTopic()) {
                String uri = entry.getUri(this.target);
                if (null != uri) {
                    n.put("linkRelated", uri);
                }
            }
        }

        this.results.put(n);
    }

    public JSONArray exec(Cypher c)
    throws CypherException
    {
        c.query(this);
        return this.results;
    }
}
