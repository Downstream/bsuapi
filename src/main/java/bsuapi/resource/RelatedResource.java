package bsuapi.resource;

import bsuapi.behavior.BehaviorType;
import bsuapi.dbal.Asset;
import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.NodeType;
import bsuapi.dbal.query.AssetTopics;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.TopicSharedRelations;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;


@Path("/related")
public class RelatedResource extends BaseResource
{
    private static final int TIMEOUT = 1000;

    @Path("/asset/{GUID}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response apiAssetRelated(
            @PathParam("GUID") String guid,
            @Context UriInfo uriInfo
    ){

        Response response = this.prepareResponse(uriInfo);

        Asset asset = new Asset(URLCoder.decode(guid));
        Cypher c = new Cypher(db);

        try {
            c.resolveNode(asset);
        } catch (Exception e) {
            return response.notFound(e.getMessage());
        }

        JSONObject result = new JSONObject();
        result.put("node", asset.toJson());

        try {
            result.put("related",this.buildAssetRelated(asset, c));
            return response.data(result, "Found :"+ asset.name() +" {"+ asset.getNodeKeyField() +":\""+ asset.getNodeKey() +"\"}");
        } catch (Exception e) {
            return response.exception(e);
        }
    }

    @Path("/{topic: [a-z]*}/{value}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response apiRelated(
        @PathParam("topic") String topic,
        @PathParam("value") String value,
        @Context UriInfo uriInfo
    ){

        //Response response = Response.prepare(new Request(uriInfo));
        Response response = this.prepareResponse(uriInfo);

        if (value == null || topic == null)
        {
            return response.badRequest("Required method parameters missing: topic label, topic name");
        }

        String searchVal = URLCoder.decode(value);
        String searchTopic = topic.substring(0, 1).toUpperCase() + topic.substring(1); // upper first
        response.setTopic(searchTopic, searchVal);

        return this.handleBehavior(BehaviorType.RELATED);
    }

    private JSONObject buildAssetRelated(Asset asset, Cypher c)
    throws CypherException
    {
        JSONObject result = new JSONObject();

        for (NodeType n : NodeType.values()) {
            if (n.isTopic()) {
                CypherQuery query = new AssetTopics(n);
                this.setQueryConfig(query);
                result.put(n.labelName(), query.exec(c));
            }
        }

        return result;
    }

    private void setQueryConfig(CypherQuery query)
    {
        query.setPage(this.getParam(CypherQuery.pageParam));
        query.setLimit(this.getParam(CypherQuery.limitParam));
        query.setHasGeo(this.getParamBool(CypherQuery.hasGeoParam));
    }
}
