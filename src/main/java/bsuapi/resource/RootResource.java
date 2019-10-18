package bsuapi.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import bsuapi.behavior.Related;
import bsuapi.dbal.*;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.TopicTop;
import org.json.JSONArray;
import org.json.JSONObject;

@Path( "/" )
public class RootResource extends BaseResource
{
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public javax.ws.rs.core.Response home(
            @Context UriInfo uriInfo
    ){

        Response response = this.prepareResponse(uriInfo);

        JSONObject data = new JSONObject();
        data.put("title","Boise State World Museum Neo4j JSON API");
        data.put("summary","Multiple RESTful URI methods to retrieve preset JSON representations of the graph of curated assets.");
        data.put("note","Project goal: >90% test coverage, and every API method has an equivalent function registered.");
        data.put("methods", this.buildMethodList());
        data.put("schema", this.buildSchema(response));

        try (
            Cypher c = new Cypher(db);
        ) {
            data.put("topics", this.topicsList(c));
        } catch (Exception e) {
            data.put("topics", new JSONObject());
        }

        return response.plain(data);
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
        JSONObject data = new JSONObject();
        data.put("uri", "/");
        data.put("description", "API HOME - you are here - a place to help you get where you're going.");
        data.put("representation", "(THIS)");
        return data;
    }

    private JSONObject topicsList(Cypher c)
    throws CypherException
    {
        JSONObject topics = new JSONObject();
        for (NodeType n : NodeType.values()) {
            if (n.isTopic()) {
                CypherQuery query = new TopicTop(n);
                query.setLimit(this.getParam("limit"));
                topics.put(n.labelName(), query.exec(c));
            }
        }
        return topics;
    }

    private JSONObject buildSchema(Response response)
    {
        JSONObject result = new JSONObject();
        for (String s : JsonSchemaResource.elements()) {
            result.put(s, JsonSchemaResource.schemaUri(response, s));
        }

        return result;
    }
}