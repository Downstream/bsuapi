package bsuapi.resource;

import bsuapi.behavior.BehaviorException;
import bsuapi.behavior.BehaviorType;
import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
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

    public javax.ws.rs.core.Response handleBehavior(BehaviorType behaviorType)
    {
        try (
            Cypher c = new Cypher(db)
        ) {
            return response.behavior(behaviorType, c);
        }
        catch (BehaviorException e)
        {
            return response.badRequest(e.getMessage());
        }
        catch (CypherException e)
        {
            return response.exception(e);
        }
    }

    public Response prepareResponse(UriInfo uriInfo)
    {
        this.response = Response.prepare(new Request(uriInfo));
        return this.response;
    }

    protected boolean getParamBool(String key)
    {
        String val = this.getParam(key);
        if (val == null) return false;

        return (
            val.equals("1") ||
            val.toLowerCase().equals("true")
        );
    }

    protected String getParam(String key)
    {
        if (null == this.response) return "";

        return this.response.getParam(key);
    }

    protected String getParam(String key, String fallback)
    {
        if (null == this.response) return fallback;

        String val = this.response.getParam(key);
        if (val == null) {
            return fallback;
        }

        return val;
    }
}
