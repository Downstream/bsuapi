package bsuapi.resource;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.script.CypherScript;
import bsuapi.dbal.script.CypherScriptFile;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;


@Path("/generate")
public class GenerationResource extends BaseResource
{
    private static final int TIMEOUT = 1000;

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

    private javax.ws.rs.core.Response doScript(Response response, CypherScript file)
    {
        CypherScriptFile script;

        try {
            script = CypherScriptFile.go(file);
        } catch (Exception e) {
            return response.exception(e);
        }

        if (script.isRunning() || script.isComplete()) {
            return response.data(script.statusReport(), script.toString());
        }

        if (script.isReady()) {
            try (
                Cypher c = new Cypher(db)
            ) {
                script.exec(c);
            }
            catch (Exception e)
            {
                return response.exception(e);
            }
        }

        return response.data(script.statusReport(), script.toString());
    }
}
