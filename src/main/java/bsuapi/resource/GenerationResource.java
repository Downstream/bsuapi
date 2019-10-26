package bsuapi.resource;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.CypherScriptFile;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;


@Path("/generate")
public class GenerationResource extends BaseResource
{
    private static final int TIMEOUT = 1000;

    @Path("/info")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response info(@Context UriInfo uriInfo){
        Response response = this.prepareResponse(uriInfo);

        HashMap<String, CypherQuery> commands = new HashMap<>();
        commands.put("clear", new CypherScriptFile("infoCardsClear"));
        commands.put("create", new CypherScriptFile("infoCards"));

        return this.executeCommands(response, commands, "infoCards.cypher");
    }

    private javax.ws.rs.core.Response executeCommands(Response response, HashMap<String, CypherQuery> commands, String messagePrefix)
    {
        try (
                Cypher c = new Cypher(db);
        ) {
            JSONObject result = new JSONObject();

            for (HashMap.Entry<String, CypherQuery> entry : commands.entrySet()) {
                result.put(entry.getKey(), entry.getValue().exec(c));
            }

            return response.data(result, messagePrefix + " graph generated");
        }
        catch (Exception e)
        {
            return response.exception(e);
        }
    }
}
