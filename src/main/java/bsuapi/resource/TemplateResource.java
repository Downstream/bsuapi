package bsuapi.resource;

import bsuapi.behavior.BehaviorDescribe;
import bsuapi.behavior.BehaviorType;
import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.NodeType;
import bsuapi.dbal.Topic;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.FolderAssets;
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
    @Path("/{GUID}")
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response apiFolderAssets(
            @PathParam("GUID") String guid,
            @Context UriInfo uriInfo
    ){

        Response response = this.prepareResponse(uriInfo);

        Topic topic = new Topic(NodeType.FOLDER, URLCoder.decode(guid));
        Cypher c = new Cypher(db);

        try {
            c.resolveNode(topic);
        } catch (Exception e) {
            return response.notFound(e.getMessage());
        }

        JSONObject result = new JSONObject();
        result.put("node", topic.toJson());

        try {
            result.put("template", this.buildTemplateAssets(topic, c));
            return response.data(result, "Found :" + topic.name() + " {" + topic.getNodeKeyField() + ":\"" + topic.getNodeKey() + "\"}");
        } catch (NullPointerException e) {
            return response.data(result, "Could not build template: "+ e.getMessage());
        } catch (Exception e) {
            return response.exception(e);
        }
    }

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
        query.setTemplateOnly(true);
        return query.exec(c);
    }

    private JSONArray buildTemplateAssets(Topic topic, Cypher c)
    throws CypherException, NullPointerException
    {
        FolderAssets query = new FolderAssets(topic);
        query.setPage(this.getParam(CypherQuery.pageParam));
        query.setLimit(this.getParam(CypherQuery.limitParam));
        query.setHasGeo(this.getParamBool(CypherQuery.hasGeoParam));
        query.setTemplateOnly(true);
        return query.exec(c);
    }

    public static BehaviorDescribe describeList()
    {
        return BehaviorDescribe.resource("/template",
            "List all faculty templates (folders with layout)."
        );
    }

    public static BehaviorDescribe describeFolder()
    {
        return BehaviorDescribe.resource("/template/{GUID}",
                "Retrieves all template and positioning info for assets in a folder, excludes assets without positioning."
        );
    }
}
