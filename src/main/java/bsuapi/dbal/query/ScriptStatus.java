package bsuapi.dbal.query;

import bsuapi.dbal.script.CypherScript;

public class ScriptStatus extends CypherQuery
implements QueryResultSingleColumn
{
    /**
     * 1: Topic label
     * 2: label to use for rel count ":Topic"
     * 3: max # of matches
     */
    protected static String query = "MATCH ("+QueryResultSingleColumn.resultColumn+":Script {name: \"%1$s\"}) RETURN "+QueryResultSingleColumn.resultColumn;
    protected CypherScript script;

    public ScriptStatus(CypherScript script) {
        super(ScriptStatus.query);
        this.script = script;
    }

    public String getCommand() {
        return this.resultQuery = String.format(
            this.initQuery,
            this.script.name()
        );
    }
}
