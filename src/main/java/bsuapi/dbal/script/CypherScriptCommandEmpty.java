package bsuapi.dbal.script;

import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.QueryResultNone;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.Result;

public class CypherScriptCommandEmpty extends CypherQuery
implements QueryResultNone
{
    public CypherScriptCommandEmpty(String command) {
        super(command);
    }

    public String getCommand()
    {
        return this.resultQuery = this.initQuery;
    }

    public void collectResult(Result result)
    {
        this.entryHandler(StringUtils.abbreviate(this.getCommand(),80));
    }
}
