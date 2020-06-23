package bsuapi.dbal.script;

import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.QueryResultSingleColumn;
import org.neo4j.graphdb.Result;
import org.neo4j.helpers.collection.Iterators;

import java.util.Iterator;

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

    public void collectResult(Result result)
    {
        Iterator<Object> resultIterator = result.columnAs(QueryResultSingleColumn.resultColumn);
        for (Object entry : Iterators.asIterable(resultIterator)) {
            this.entryHandler(entry);
        }
    }
}
