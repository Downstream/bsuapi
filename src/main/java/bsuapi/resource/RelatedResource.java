package bsuapi.resource;

import bsuapi.dbal.Topic;
import bsuapi.dbal.NodeUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.string.UTF8;

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

    @Path("/{topic: [a-zA-Z][a-zA-Z_0-9]*}/{value: [a-zA-Z][a-zA-Z_0-9]*}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response apiRelated(
            @PathParam("topic") String topic,
            @PathParam("value") String value
    ){
        JSONObject res = new JSONObject();
        res.put("success", "false");
        res.put("message", "could not find a matching topic");

        if (value == null || topic == null)
        {
            res.put("data", new JSONObject());
            return Response.status( Response.Status.NOT_ACCEPTABLE ).entity( UTF8.encode(res.toString()) ).build();
        }

        try ( Transaction tx = db.beginTx() ) {
            JSONObject data = this.findByLabelIndex(topic, value);
            res.put("success", "true");
            res.put("message", "Found "+ data.get("count") +" "+ topic +" matches for "+ value );
            res.put("data", data);
            tx.success();
            return Response.status( Response.Status.OK ).entity( UTF8.encode(res.toString()) ).build();
        }
        catch (Exception e)
        {
            res.put("success", "false");
            res.put("message", e.getMessage());
            res.put("data", e.toString());
        }

        return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( UTF8.encode(res.toString()) ).build();
    }

    private JSONObject findByLabelIndex(
            String topic,
            String value
    ){
        JSONObject data = new JSONObject();
        JSONArray matches = new JSONArray();

        Topic l = new Topic(db, topic);

        for (Node node : l.findRelated(value)) {
            matches.put(NodeUtil.toJsonObject(node));
        }

        data.put("topic", topic);
        data.put("count", matches.length());
        data.put("matches", matches);
        return data;
    }
}
