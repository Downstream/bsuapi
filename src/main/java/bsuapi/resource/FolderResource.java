package bsuapi.resource;

import bsuapi.behavior.BehaviorType;
import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.query.CypherQuery;
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


@Path("/folder")
public class FolderResource extends BaseResource
{
    private static final int TIMEOUT = 1000;

    @Path("/{guid}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response apiFolderData(
            @PathParam("guid") String guid,
            @Context UriInfo uriInfo
    ){
        if (guid == null)
        {
            return this.apiFolders(uriInfo);
        }

        Response response = this.prepareResponse(uriInfo);

        String searchVal = URLCoder.decode(guid);
        response.setTopic("Folder", searchVal);

        return this.handleBehavior(BehaviorType.FOLDER);
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
            data.put("folders", this.getFolderList(c));
        } catch (Exception e) {
            data.put("folders", new JSONArray());
            data.put("warning", "No Folders Found");
        }

        return response.plain(data);
    }

    public JSONArray getFolderList(Cypher c)
    throws CypherException
    {
        CypherQuery query = new FolderList();
        query.setPage(this.getParam(CypherQuery.pageParam));
        query.setLimit(this.getParam(CypherQuery.limitParam));
        return query.exec(c);
    }
}
