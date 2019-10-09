package bsuapi.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import bsuapi.behavior.Related;
import bsuapi.dbal.NodeType;
import bsuapi.dbal.Topic;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.string.UTF8;

@Path( "/" )
public class RootResource
{
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public Response home()
    {

        JSONObject res = new JSONObject();
        res.put("title","Boise State World Museum Neo4j JSON API");
        res.put("summary","Multiple RESTful URI methods to retrieve preset JSON representations of the graph of curated assets.");
        res.put("note","Project goal: >90% test coverage, and every API method has an equivalent function registered.");
        res.put("topics", this.topicsList());
        res.put("methods", this.buildMethodList());

        return Response.status( Status.OK ).entity( UTF8.encode( res.toString(4) ) ).build();
    }

    private JSONArray buildMethodList()
    {
        JSONArray methods = new JSONArray();
        methods.put(this.youarehere());

        methods.put(Related.describe());

        return methods;
    }

    private JSONObject youarehere()
    {
        JSONObject res = new JSONObject();
        res.put("uri", "/");
        res.put("description", "API HOME - you are here - a place to help you get where you're going.");
        res.put("representation", "(THIS)");
        return res;
    }

    private JSONArray topicsList()
    {
        JSONArray topics = new JSONArray();
        for (NodeType n : NodeType.values()) {
            if (n.isTopic()) {
                topics.put(n.labelName());
            }
        }
        return topics;
    }
}