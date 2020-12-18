package bsuapi.resource;

import bsuapi.behavior.BehaviorDescribe;
import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.Node;
import bsuapi.dbal.NodeType;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.QueryResultSingleColumn;
import bsuapi.dbal.script.CypherScript;
import bsuapi.dbal.script.CypherScriptCommandSingle;
import bsuapi.service.ScriptExecutor;
import bsuapi.service.ScriptOverseer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.cypher.internal.frontend.v2_3.ast.In;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Map;

@Path("/sync_status")
public class SyncStatusResource extends BaseResource
{
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response status(
        @Context UriInfo uriInfo
    )
    {
        Response response = this.prepareResponse(uriInfo);

        try (
                Cypher c = new Cypher(db)
        ) {
            JSONObject status = new JSONObject();

            CypherScript script = CypherScript.OPENPIPE_SYNC;
            JSONObject report = ScriptOverseer.report(c, script);
            Node api = this.getConfigApi(c);

            int perPage;
            try {
                Long assetsPerPage = (Long) api.getRawProperty("assetsPerPage");
                perPage = assetsPerPage.intValue();
            } catch (Throwable e) {
                perPage = 100;
            }

            int lastPage = Integer.parseInt(report.optString("page","0"));
            int pageNumberBy10 = ((perPage*(lastPage-1))/10)+1;

            String lastRun = this.bestLastRunDate(api, report);

            status.put("lastPageOfSync", api.getRawProperty("allAssets") + "?changeStart=" + lastRun + "&ps=" + api.getRawProperty("assetsPerPage") + "&p=" + (lastPage -1));
            status.put("nextPageOfSync", api.getRawProperty("allAssets") + "?changeStart=" + lastRun + "&ps=" + api.getRawProperty("assetsPerPage") + "&p=" + lastPage);
            status.put("nearCauseIfIssue",api.getRawProperty("allAssets") + "?changeStart=" + lastRun + "&ps=10&p=" + pageNumberBy10);
            status.put("config", api.toJsonObject());
            status.put("report", report);

            return response.data(status, "Report of last run of main openpipe sync, and attempt to identify any failure.");
        }
        catch (Exception e)
        {
            return response.exception(e);
        }
    }

    private String bestLastRunDate(Node api, JSONObject report)
    {
        String reportRun = report.optString("lastRun");
        if (reportRun != null) {
            return reportRun;
        }

        Object apiRun = api.getRawProperty("lastRun");
        if (apiRun instanceof String) {
            return (String) apiRun;
        }

        return "2020-01-01";
    }

    private Node getConfigApi(Cypher c)
    throws CypherException
    {
        try {
            return c.querySingleNode("MATCH (api:OpenPipeConfig {name: 'api'}) RETURN api", "api");
        } catch (Throwable e) {
            throw new CypherException("Could not retrieve openpipe sync config.", e);
        }
    }

    public static BehaviorDescribe describe()
    {
        BehaviorDescribe desc = BehaviorDescribe.resource("/sync_status",
            "Retrieve the configuration and last run details of the OpenPipe sync, along with a reference to where the sync stopped."
        );

        return desc;
    }
}
