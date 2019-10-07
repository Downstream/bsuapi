package bsuapi.dbal.query;

public class CypherQuery {
    protected String[] args;
    protected String initQuery;
    protected String resultQuery;

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
}
