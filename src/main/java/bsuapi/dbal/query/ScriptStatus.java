package bsuapi.dbal.query;

public class ScriptStatus extends CypherQuery
implements QueryResultSingleColumn
{
    /**
     * 1: Topic label
     * 2: label to use for rel count ":Topic"
     * 3: max # of matches
     */
    protected static String query = "MATCH ("+QueryResultSingleColumn.resultColumn+":Script) RETURN "+QueryResultSingleColumn.resultColumn;

    public ScriptStatus() {
        super(ScriptStatus.query);
    }

    public String getCommand() {
        return this.resultQuery = this.initQuery;
    }
}
