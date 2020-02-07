package bsuapi.dbal.query;

import bsuapi.dbal.Topic;
import bsuapi.resource.Util;

public class CypherScriptFile extends CypherQuery
{
    protected Topic topic;

    public CypherScriptFile(String filename) {
        super(Util.readResourceFile(filename+".cypher"));
    }

    public String getCommand()
    {
        return this.resultQuery = this.initQuery;
    }
}
