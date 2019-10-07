package bsuapi.resource;

import bsuapi.behavior.Related;
import bsuapi.dbal.Cypher;
import bsuapi.dbal.JsonResponse;
import bsuapi.dbal.Topic;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;

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

    @Context
    public Log log;

    private static final int TIMEOUT = 1000;

    // @todo: refactor to separate request/response handling from behavior
    @Path("/{topic: [a-z]*}/{value}")
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

        String searchVal = URLCoder.decode(value);
        String searchTopic = topic.substring(0, 1).toUpperCase() + topic.substring(1); // upper first

        try (
                Cypher c = new Cypher(db);
        ) {
            Topic t = new Topic(searchTopic, searchVal);
            c.resolveTopic(t);

            log.info("Related search: :"+ searchTopic +" \""+ searchVal+"\"");
            if (!t.hasMatch())
            {
                log.info("No match "+ t.toString());
                return JsonResponse.notFound();
            } else {
                Related rel = new Related(t);
                rel.resolveBehavior(c);
                log.info("Related result: "+ rel.message);
                //rel.debug(log);
                return JsonResponse.data(rel.toJson(), rel.message);
            }
        }
        catch (Exception e)
        {
            return JsonResponse.exception(e);
        }
    }
}
