package bsuapi.dbal.script;

import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.QueryResultSingleColumn;

public class CypherScriptCommandSingle extends CypherQuery
implements QueryResultSingleColumn
{
    public CypherScriptCommandSingle(String command) {
        super(command);
    }

    public String getCommand()
    {
        return this.resultQuery = this.initQuery;
    }
}
