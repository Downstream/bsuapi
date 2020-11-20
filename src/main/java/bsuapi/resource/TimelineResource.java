package bsuapi.resource;

import bsuapi.behavior.BehaviorType;
import bsuapi.dbal.*;
import bsuapi.dbal.query.AssetTopics;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.QueryResultCollector;
import bsuapi.dbal.query.Timeline;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;


@Path("/timeline")
public class TimelineResource extends BaseResource
{
    private static final int TIMEOUT = 1000;

    @Path("/folder/{GUID}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response apiFolderTimeline(
            @PathParam("GUID") String guid,
            @Context UriInfo uriInfo
    ){

        Response response = this.prepareResponse(uriInfo);

        Folder folder = new Folder(URLCoder.decode(guid));
        Cypher c = new Cypher(db);

        try {
            c.resolveNode(folder);
        } catch (Exception e) {
            return response.notFound(e.getMessage());
        }

        JSONObject result = new JSONObject();
        result.put("node", folder.toJson());

        try {
            result.put("timeline",this.buildFolderTimeline(folder, c));
            return response.data(result, "Found :"+ folder.name() +" {"+ folder.getNodeKeyField() +":\""+ folder.getNodeKey() +"\"}");
        } catch (Exception e) {
            return response.exception(e);
        }
    }

    @Path("/{topic: [a-z]*}/{GUID}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response apiRelated(
        @PathParam("topic") String topic,
        @PathParam("GUID") String guid,
        @Context UriInfo uriInfo
    ){

        if (topic.equals("folder")) return this.apiFolderTimeline(guid, uriInfo);

        //Response response = Response.prepare(new Request(uriInfo));
        Response response = this.prepareResponse(uriInfo);

        if (guid == null || topic == null)
        {
            return response.badRequest("Required method parameters missing: topic label, topic guid");
        }

        String searchVal = URLCoder.decode(guid);
        String searchTopic = topic.substring(0, 1).toUpperCase() + topic.substring(1); // upper first
        response.setTopic(searchTopic, searchVal);

        return this.handleBehavior(BehaviorType.RELATED);
    }

    private JSONObject buildFolderTimeline(Folder folder, Cypher c)
    throws CypherException
    {
        Timeline query = new Timeline(folder);
        this.setQueryConfig(query);
        c.query(query);
        return query.getResults();
    }

    private void setQueryConfig(CypherQuery query)
    {
        query.setPage(this.getParam(CypherQuery.pageParam));
        query.setLimit(this.getParam(CypherQuery.limitParam));
        query.setHasGeo(this.getParamBool(CypherQuery.hasGeoParam));
    }
}
