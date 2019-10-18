package bsuapi.resource;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.Topic;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

abstract public class BaseResource
{
    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    protected Response response;

    public Topic prepareTopic(Cypher c, String label, String key)
    throws CypherException
    {
        Topic t = new Topic(label, key);
        c.resolveTopic(t);

        log.info(this.getClass().getSimpleName() + " search: :"+ label +" \""+ key+"\"");
        return t;
    }

    public Response prepareResponse(UriInfo uriInfo)
    {
        return this.response = Response.prepare(new Request(uriInfo));
    }

    protected String getParam(String key)
    {
        return this.response.getParam(key);
    }
}
