package bsuapi.resource;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.query.CypherScriptFile;

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

        return this.doScript(response, "infoCards.cypher");
    }

    @Path("/openpipe")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response openpipe(@Context UriInfo uriInfo)
    {
        Response response = this.prepareResponse(uriInfo);

        return this.doScript(response, "openpipe.cypher");
    }

    private javax.ws.rs.core.Response doScript(Response response, String filename)
    {
        CypherScriptFile script;

        try {
            script = CypherScriptFile.go(filename);
        } catch (Exception e) {
            return response.exception(e);
        }

        String msg = " already in progress.";
        if (!script.isRunning()) {
            try (
                Cypher c = new Cypher(db)
            ) {
                script.exec(c);
                msg = " started.";
            }
            catch (Exception e)
            {
                return response.exception(e);
            }
        }

        return response.data(script.statusReport(), script.toString() + msg);
    }
}
