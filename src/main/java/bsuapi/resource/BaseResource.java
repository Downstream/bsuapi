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

    public Topic sanitizedTopic(Cypher c, String label, String key)
    throws CypherException
    {
        label = label.substring(0, 1).toUpperCase() + label.substring(1);
        key = URLCoder.decode(key);

        return this.prepareTopic(c, label, key);
    }

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

    protected String getParam(String key, String fallback)
    {
        String val = this.response.getParam(key);
        if (val == null) {
            return fallback;
        }

        return val;
    }
}
