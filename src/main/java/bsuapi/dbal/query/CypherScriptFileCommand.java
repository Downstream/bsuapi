package bsuapi.dbal.query;

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
