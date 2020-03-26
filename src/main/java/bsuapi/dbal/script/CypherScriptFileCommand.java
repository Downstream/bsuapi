package bsuapi.dbal.script;

import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.QueryResultSingleColumn;

public class CypherScriptFileCommand extends CypherQuery
implements QueryResultSingleColumn
{
    public CypherScriptFileCommand(String command) {
        super(command);
    }

    public String getCommand()
    {
        return this.resultQuery = this.initQuery;
    }
}
