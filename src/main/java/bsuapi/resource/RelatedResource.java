package bsuapi.resource;

import bsuapi.behavior.Related;
import bsuapi.dbal.JsonResponse;
import bsuapi.dbal.Topic;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/related")
public class RelatedResource
{
    @Context
    public GraphDatabaseService db;

    private static final int TIMEOUT = 1000;

    @Path("/{topic: [a-z]*}/{value: [a-zA-Z][a-zA-Z_0-9]*}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response apiRelated(
            @PathParam("topic") String topic,
            @PathParam("value") String value
    ){
        if (value == null || topic == null)
        {
            return JsonResponse.badRequest("Required method parameters missing: topic label, topic name");
        }

        String searchVal = value.replace('_',' '); // underscores to spaces
        String searchTopic = topic.substring(0, 1).toUpperCase() + topic.substring(1); // upper first

        try ( Transaction tx = db.beginTx() )
        {
            Topic t = new Topic(db, searchTopic, searchVal);
            tx.success();

            if (!t.hasMatch())
            {
                return JsonResponse.notFound();
            } else {
                Related rel = new Related(t);
                return JsonResponse.data(rel.toJson(), rel.message);
            }
        }
        catch (Exception e)
        {
            return JsonResponse.exception(e);
        }
    }
}
