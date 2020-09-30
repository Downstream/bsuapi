package bsuapi.resource;

import bsuapi.behavior.BehaviorDescribe;
import bsuapi.dbal.Cypher;
import bsuapi.dbal.script.CypherScript;
import bsuapi.service.ScriptExecutor;
import bsuapi.service.ScriptOverseer;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/execute")
public class ExecutorResource extends BaseResource
{
    @Path("/{scriptName: [A-Za-z_]*}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response report(
        @PathParam("scriptName") String scriptName,
        @Context UriInfo uriInfo
    )
    {
        Response response = this.prepareResponse(uriInfo);

        try (
                Cypher c = new Cypher(db)
        ) {
            CypherScript script = CypherScript.valueOf(scriptName.toUpperCase());
            JSONObject data = this.scriptReport(c, script);

            return response.data(data, "Report of last run of CypherScript: "+ script.name());
        }
        catch (Exception e)
        {
            return response.exception(e);
        }
    }

    @Path("/{scriptName: [A-Za-z_]*}/start")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response start(
            @PathParam("scriptName") String scriptName,
            @Context UriInfo uriInfo
    )
    {
        Response response = this.prepareResponse(uriInfo);

        try (
                Cypher c = new Cypher(db)
        ) {
            CypherScript script = CypherScript.valueOf(scriptName.toUpperCase());
            JSONObject data = this.scriptStart(c, script);

            return response.data(data, "Starting or checking CypherScript: "+ script.name());
        }
        catch (Exception e)
        {
            return response.exception(e);
        }
    }

    private JSONObject scriptStart(Cypher c, CypherScript script)
    {
        return ScriptExecutor.exec(c, script).statusReport();
    }

    private JSONObject scriptReport(Cypher c, CypherScript script)
    {
        JSONObject result;
        if (ScriptOverseer.has(script.name())) {
            result = script.getRunner(c).statusReport();
        } else {
            result = script.getStoredReport(c);
            result.remove("keyField");
            result.remove("keyRaw");
            result.remove("keyEncoded");
            result.put("next", "Command ready to be started.");
        }

        return result;
    }

    public static BehaviorDescribe describe()
    {
        BehaviorDescribe desc = BehaviorDescribe.resource("/execute/{SCRIPT}",
            "Retrieve the status of, or start, pre-defined long-running cypher scripts."
        );

        desc.arg("SCRIPT", "name of script to run.");
        desc.put("args", CypherScript.describeAll());
        desc.put("uri-start", "/execute/{SCRIPT}/start");

        return desc;
    }
}
