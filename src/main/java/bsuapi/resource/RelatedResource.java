package bsuapi.resource;

import bsuapi.behavior.BehaviorType;
import bsuapi.dbal.Cypher;

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

        try (
                Cypher c = new Cypher(db)
        ) {
            return response.behavior(BehaviorType.RELATED, c);
        }
        catch (Exception e)
        {
            return response.exception(e);
        }

    }
}
