package bsuapi.resource;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.script.CypherScript;
import bsuapi.dbal.script.CypherScriptAbstract;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/generate")
public class GenerationResource extends BaseResource
{
    @Path("/info")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response info(@Context UriInfo uriInfo)
    {
        Response response = this.prepareResponse(uriInfo);

        return this.doScript(response, CypherScript.INFO);
    }

    @Path("/openpipe/rebuild")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response openpipe(@Context UriInfo uriInfo)
    {
        Response response = this.prepareResponse(uriInfo);

        return this.doScript(response, CypherScript.OPENPIPE_REBUILD);
    }

    private javax.ws.rs.core.Response doScript(Response response, CypherScript script)
    {
        CypherScriptAbstract runner;

        try {
            this.log.info("Starting CypherScript "+script);
        } catch (Exception e) {
            this.log.error("Could not load CypherScript "+script, e);
            return response.exception(e);
        }

        try (
            Cypher c = new Cypher(db)
        ) {
            runner = script.getRunner(c);
            return response.data(runner.statusReport(), script.toString());
        }
        catch (Exception e)
        {
            return response.exception(e);
        }
    }
}
