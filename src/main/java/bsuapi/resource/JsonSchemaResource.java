package bsuapi.resource;

import bsuapi.dbal.NodeType;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path( "/schema" )
public class JsonSchemaResource extends BaseResource
{
    private Response response;

    public static String[] elements()
    {
        return new String[]{"related", "topic", "asset"};
    }

    @Path("/related.schema.json")
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public javax.ws.rs.core.Response related(
            @Context UriInfo uriInfo
    ){
        this.response = this.prepareResponse(uriInfo);

        JSONObject schema = this.standardResponse("related", "For an existing topic, provide a sample of assets, and the most relevant topics for each type of topic.");
        schema.put("title", "Related Topics");

        JSONArray required = new JSONArray();
        required.put("success");
        required.put("message");
        required.put("data");
        schema.put("required", required);

        JSONObject properties = new JSONObject();
        properties.put("success", this.prop("boolean", "Whether or not there was a failure or problem retrieving a complete dataset."));
        properties.put("message", this.prop("string", "User friendly message describing the results, or errors."));
        properties.put("data", this.relatedData());
        schema.put("properties", properties);

        return response.plain(schema);
    }

    @Path("/topic.schema.json")
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public javax.ws.rs.core.Response topic(
            @Context UriInfo uriInfo
    ){
        this.response = this.prepareResponse(uriInfo);

        JSONObject schema = this.standardResponse("topic", "A specific node, of a given type. e.g.: a specific Artist");
        schema.put("title", "Topic");

        JSONObject properties = new JSONObject();
        properties.put("keyField", this.prop("string", "Property name of this topic instance."));
        properties.put("keyEncoded", this.prop("string", "Property name of this topic instance."));
        properties.put("keyRaw", this.prop("string", "Property name of this topic instance."));
        properties.put("name", this.prop("string", "Property name of this topic instance."));
        properties.put("guid", this.prop("string", "Key for all topics."));
        properties.put("linkRelated", this.prop("string", "A relative URL to this topic's 'related' dataset"));
        properties.put("linkAssets", this.prop("string", "A relative URL to an iterable collection of assets for this topic."));
        properties.put("linkTimeline", this.prop("string", "A relative URL to a timeline of assets for the topic."));

        schema.put("properties", properties);

        return response.plain(schema);
    }

    @Path("/asset.schema.json")
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public javax.ws.rs.core.Response asset(
            @Context UriInfo uriInfo
    ){
        this.response = this.prepareResponse(uriInfo);

        JSONObject schema = this.standardResponse("asset", "An image asset, usually representing a specific piece of art, along with urls to representative images.");
        schema.put("title", "Asset");

        JSONObject properties = new JSONObject();
        properties.put("name", this.prop("string", "Friendly 'name' of the asset"));
        properties.put("title", this.prop("string", "Display name given to the asset."));
        properties.put("primaryImageSmall", this.prop("string", "Full url to a JPG image of the asset ~1200px"));

        schema.put("properties", properties);

        return response.plain(schema);
    }

    private JSONObject standardResponse(String element, String description)
    {

        JSONObject schema = new JSONObject();
        schema.put("$id", JsonSchemaResource.schemaUri(this.response, element));
        schema.put("$schema", "http://json-schema.org/draft-07/schema#");
        schema.put("description", description);
        schema.put("type", "object");

        return schema;
    }

    private JSONObject prop(String type, String desc)
    {
        JSONObject x = new JSONObject();
        x.put("type", type);
        x.put("description", desc);
        return x;
    }

    private JSONObject ref(String element)
    {
        JSONObject x = new JSONObject();
        x.put("$ref", JsonSchemaResource.schemaUri(this.response, element));
        return x;
    }

    private JSONObject array(String type)
    {
        JSONObject x = new JSONObject();
        JSONObject a = new JSONObject();
        a.put("type", type);
        x.put("type", "array");
        x.put("items", a);
        return x;
    }

    private JSONObject arrayRef(String element, String desc)
    {
        JSONObject x = new JSONObject();
        x.put("type", "array");
        x.put("description", desc);
        x.put("items", this.ref(element));
        return x;
    }

    private JSONObject relatedData()
    {
        JSONObject x = new JSONObject();
        x.put("node", this.ref("topic"));
        x.put("assets", this.arrayRef("asset", "Set of assets directly related to this topic."));
        x.put("topic", this.prop("string", "Type of topic node matched, e.g.: Artist, Tag, ..."));

        JSONObject r = new JSONObject();
        for (NodeType n : NodeType.values()) {
            if (n.isTopic()) {
                r.put(n.labelName(), this.arrayRef("topic", "List of related "+ n.labelName() + " ordered by number of shared relationships. (most related first)"));
            }
        }

        x.put("related", r);
        return x;
    }

    public static String schemaUri(Response response, String element)
    {
        return response.buildUri("/schema/"+ element +".schema.json");
    }
}
