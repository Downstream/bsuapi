package bsuapi.resource;

import bsuapi.behavior.BehaviorDescribe;
import bsuapi.behavior.BehaviorType;
import bsuapi.dbal.*;
import bsuapi.dbal.query.*;
import org.apache.commons.lang3.ObjectUtils;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.time.LocalDate;


@Path("/timeline")
public class TimelineResource extends BaseResource
{
    private static final int TIMEOUT = 1000;

    @Path("/{label: [a-z]*}/{GUID}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response apiRelated(
        @PathParam("label") String label,
        @PathParam("GUID") String guid,
        @Context UriInfo uriInfo
    ){

        Response response = this.prepareResponse(uriInfo);

        Topic topic = new Topic(NodeType.match(label), URLCoder.decode(guid));
        Cypher c = new Cypher(db);

        try {
            c.resolveNode(topic);
        } catch (Exception e) {
            return response.notFound(e.getMessage());
        }

        JSONObject result = new JSONObject();
        result.put("node", topic.toJson());

        try {
            JSONObject timeline = this.buildTimeline(topic, c);
            result.put("timeline", timeline);
            return response.data(result, "Found :" + topic.name() + " {" + topic.getNodeKeyField() + ":'" + topic.getNodeKey() + "'}");
        } catch (NullPointerException e) {
            return response.data(result, "Could not build timeline: "+ e.getMessage());
        } catch (Exception e) {
            return response.exception(e);
        }
    }

    private JSONObject buildTimeline(Topic topic, Cypher c)
    throws CypherException, NullPointerException
    {
        Timeline query = new Timeline(topic);
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

    public static BehaviorDescribe describe()
    {
        BehaviorDescribe desc = BehaviorDescribe.resource("/timeline/{topic}/{GUID}",
            "List all assets for that (topic|folder), grouped in time." +
            "Attempts to create ~100 time groups, from time-steps extrapolated from the (topic|folder) start and end dates (earliest and latest asset dates)."
        );

        desc.arg("topic", ".");
        desc.arg("GUID", "URL-encoded string, defined from source. Must start with a letter, a-zA-Z.");

        JSONObject examples = new JSONObject();
        examples.put("folder", "/bsuapi/timeline/folder/200%2F99");
        examples.put("artist", "/bsuapi/timeline/artist/b00%2F414129");

        return desc;
    }
}
