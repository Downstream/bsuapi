package bsuapi.dbal.query;

import bsuapi.dbal.CypherException;
import org.neo4j.graphdb.Result;

public interface QueryResultCollector {
    String getCommand();
    public void collectResult (Result result)
        throws CypherException;
}
