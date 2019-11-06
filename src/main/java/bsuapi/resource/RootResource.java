package bsuapi.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import bsuapi.behavior.Assets;
import bsuapi.behavior.Related;
import bsuapi.behavior.SearchBehavior;
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
        data.put("parameters", this.buildParamList());
        data.put("schema", this.buildSchema(response));
        this.attachPackageDetails(data);

        try (
            Cypher c = new Cypher(db);
        ) {
            data.put("topics", this.topicsList(c));
        } catch (Exception e) {
            data.put("topics", new JSONObject());
        }

        return response.plain(data);
    }

    private JSONObject buildMethodList()
    {
        JSONObject methods = new JSONObject();

        methods.put("root", this.youarehere());
        methods.put("related", Related.describe());
        methods.put("topic-assets", Assets.describe());
        methods.put("search", SearchBehavior.describe());

        return methods;
    }

    private JSONObject buildParamList()
    {
        JSONObject params = new JSONObject();
        params.put("limit", "(int) default:20 ignored:<1 - max number of results in a set :: /related will return 20 assets, and 20 of each topic-type.");
        params.put("page", "(int) default:1 ignored:<1 - paginated results according to limit, sets beyond the last page will be omitted :: limit=2&page=5 will return result entries 9 and 10 for each set (counting from 1).");
        params.put("requestToken", "(any urlencoded str) - will include the same token in the response body :: requestToken=abc123 => response: { \"requestToken\": \"abc123\"}");

        return params;
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
                query.setPage(this.getParam("page"));
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

    private void attachPackageDetails(JSONObject data)
    {
        data.put("version", Config.getDefault("version", "0.1"));
        data.put("package", Config.getDefault("package", "bsuapi"));
        data.put("canonical", Config.buildUri("/"));
    }
}