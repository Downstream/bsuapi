package bsuapi.dbal.query;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.Node;
import bsuapi.dbal.NodeType;
import org.json.JSONArray;
import org.json.JSONObject;

public class CypherQuery {
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

    public static CypherQuery parameterized (String parametrizedQuery, String... args)
    {
        CypherQuery q = new CypherQuery(parametrizedQuery);
        q.args = args;
        q.resultQuery = String.format(q.initQuery, (Object) args);

        return q;
    }

    public String getCommand()
    {
        return this.resultQuery;
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
