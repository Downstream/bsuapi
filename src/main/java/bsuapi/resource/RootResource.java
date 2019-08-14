package bsuapi.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
        res.put("summary","Multiple RESTful URI methods to retrieve preset JSON representations of the graph of curated artworks.");
        res.put("methods", this.buildMethodList());

        return Response.status( Status.OK ).entity( UTF8.encode( res.toString(4) ) ).build();
    }

    private JSONArray buildMethodList()
    {
        JSONArray methods = new JSONArray();
        methods.put(this.buildMethod(
            "/",
            "API HOME - you are here - a place to help you get where you're going.",
            "(THIS)")
        );
        methods.put(this.buildMethod(
                "/related/{TOPIC}/{VALUE}",
                "Find all (TOPIC)s with an indexed value matching (VALUE), along with a" +
                "collection of closely related Topics, and a collection of Artwork which references that Topic.",
                "(THIS)")
        );
        return methods;
    }

    private JSONObject buildMethod(String uri, String description, String representation)
    {
        JSONObject res = new JSONObject();
        res.put("uri", uri);
        res.put("description", description);
        res.put("representation", representation);
        return res;
    }
}