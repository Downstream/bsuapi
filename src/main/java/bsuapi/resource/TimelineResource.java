package bsuapi.resource;

import bsuapi.behavior.BehaviorDescribe;
import bsuapi.behavior.BehaviorType;
import bsuapi.dbal.*;
import bsuapi.dbal.query.*;
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
            result.put("dateStart", topic.getNodeProperty("dateStart"));
            result.put("dateEnd", topic.getNodeProperty("dateEnd"));
            result.put("dateStartRaw", topic.getNode().getRawProperty("dateStart"));
            result.put("dateEndRaw", topic.getNode().getRawProperty("dateEnd"));
            result.put("localDateStart", LocalDate.parse(topic.getNodeProperty("dateStart")));
            result.put("localDateEnd", LocalDate.parse(topic.getNodeProperty("dateEnd")));
            result.put("timeline", this.buildTimeline(topic, c));
            return response.data(result, "Found :" + topic.name() + " {" + topic.getNodeKeyField() + ":\"" + topic.getNodeKey() + "\"}");
//        } catch (Exception e) {
//            return response.exception(e);
//        }
        } catch (Throwable e) {
            return response.plain(JsonResponse.exceptionDetailed(e));
        }
    }

    private JSONObject buildTimeline(Topic topic, Cypher c)
    throws CypherException
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
        examples.put("folder", "/bsuapi/timeline/folder/http%3A%2F%2Fmec402.boisestate.edu%2Fcgi-bin%2Fopenpipe%2Fdata%2Ffolder%2F28");
        examples.put("artist", "/bsuapi/timeline/artist/http%3A%2F%2Fmec402.boisestate.edu%2Fcgi-bin%2Fopenpipe%2Fdata%2Fartist%2F193");

        return desc;
    }
}
