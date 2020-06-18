package bsuapi.resource;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.script.CypherScript;
import bsuapi.dbal.script.CypherScriptAbstract;
import bsuapi.service.ScriptExecutor;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.concurrent.Executors;

@Path("/execute")
public class ExecutorResource extends BaseResource
{
    //@Path("/script/{filename: [a-z]*}")
    @Path("/test1")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response openpipe(
        @Context UriInfo uriInfo
    )
    {
        CypherScriptAbstract script;
        Response response = this.prepareResponse(uriInfo);

        try (
                Cypher c = new Cypher(db)
        ) {
            script = ScriptExecutor.exec(c, CypherScript.INFO);
        }
        catch (Exception e)
        {
            return response.exception(e);
        }

        return response.data(script.statusReport(), "test complete");
    }
}
