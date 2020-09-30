package bsuapi.resource;

import bsuapi.behavior.BehaviorDescribe;
import bsuapi.behavior.BehaviorType;
import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.query.FolderList;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;


@Path("/template")
public class TemplateResource extends BaseResource
{
    private static final int TIMEOUT = 1000;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response apiFolders(
            @Context UriInfo uriInfo
    ){
        Response response = this.prepareResponse(uriInfo);
        JSONObject data = new JSONObject();

        try (
                Cypher c = new Cypher(db)
        ) {
            data.put("templates", this.getFolderList(c));
        } catch (Exception e) {
            data.put("templates", new JSONArray());
            data.put("warning", "No Templates Found");
        }

        return response.plain(data);
    }

    public JSONArray getFolderList(Cypher c)
    throws CypherException
    {
        FolderList query = new FolderList();
        query.setPage(this.getParam(FolderList.pageParam));
        query.setLimit(this.getParam(FolderList.limitParam));
        query.setHasGeo(this.getParamBool(FolderList.hasGeoParam));
        query.setTemplateOnly(this.getParamBool(FolderList.templateOnlyParam));
        return query.exec(c);
    }

    public static BehaviorDescribe describeList()
    {
        return BehaviorDescribe.resource("/template",
            "List all faculty templates (folders with layout)."
        );
    }
}
