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
        properties.put("guid", this.prop("string", "OpenPipe generated key for a specific topic."));
        properties.put("dateStart", this.prop("string", "YYYY-MM-DD oldest asset in this topic."));
        properties.put("dateEnd", this.prop("string", "YYYY-MM-DD latest asset in this topic."));
        properties.put("hasLayout", this.prop("boolean", "(optional) if present, and true - contained assets have positional details available from the linkTemplate URI."));
        properties.put("smallImage", this.prop("string", "URI to an image representative of this topic."));
        properties.put("linkRelated", this.prop("string", "A relative URL to this topic's 'related' dataset"));
        properties.put("linkAssets", this.prop("string", "A relative URL to an iterable collection of assets for this topic."));
        properties.put("linkTimeline", this.prop("string", "A relative URL to a timeline of assets for the topic."));
        properties.put("linkTemplate", this.prop("string", "(optional) A relative URL to assets for this topic/folder, including all asset positional data. Excludes assets without position. Only included on folders which have a layout and a template."));

        properties.put("biography", this.prop("string", "[NOT YET IMPLEMENTED] Long text describing the topic."));

        JSONObject type = this.prop("string", "Type of node retrieved.");
        JSONArray typeVals = new JSONArray();
        for ( NodeType n : NodeType.values() ) {
            typeVals.put(n.toString());
        }
        type.put("enum", typeVals);
        properties.put("type", type);

        schema.put("properties", properties);
        schema.put("required", new JSONArray(new String[]{
                "title",
                "type",
                "dateStart",
                "dateEnd",
                "guid",
                "keyField",
                "keyRaw",
                "keyEncoded",
                "linkRelated",
                "linkAssets",
                "linkTimeline",
                "smallImage"
        }));

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
        properties.put("date", this.prop("string", "YYYY-MM-DD date of creation of the object. Negative denotes BC: -0001 is 2 BC"));
        properties.put("openpipe_date", this.prop("string", "Openpipe date format (CE/BC YYYY MMM DD hh:mm:ss) date of creation of the object. e.g.:\"CE 1872 JAN 01 00:00:00\""));
        properties.put("guid", this.prop("string", "OpenPipe generated GUID for this asset."));
        properties.put("keyField", this.prop("string", "The name of the property containing the primary key for this asset"));
        properties.put("keyRaw", this.prop("string", "Raw value of the key"));
        properties.put("keyEncoded", this.prop("string", "URL-encoded key, for use in most API methods."));
        properties.put("linkRelated", this.prop("string", "URL which retrieves all related Topics for this asset."));
        properties.put("type", this.prop("string", "Type of node retrieved. Always \"asset\""));

        properties.put("primaryImageSmall", this.prop("string", "Full url to a JPG image of the asset ~1200px"));
        properties.put("primaryImageFull", this.prop("string", "Full url to the max resolution image of the asset"));
        properties.put("primaryImageThumb", this.prop("string", "[NOT YET IMPLEMENTED] Full url to a small resolution image of the asset (largest dimension 300px)"));
        properties.put("primaryImageSmallDimensions", this.prop("string", "Pixels dimensions of the image in pixels: width,height. eg: \"100,200\" for a 200px tall image"));
        properties.put("primaryImageFullDimensions", this.prop("string", "Pixels dimensions of the image in pixels: width,height. eg: \"100,200\" for a 200px tall image"));
        properties.put("primaryImageThumbDimensions", this.prop("string", "[NOT YET IMPLEMENTED] Pixels dimensions of the image in pixels: width,height. eg: \"100,200\" for a 200px tall image"));

        properties.put("openpipe_dimensions", this.array("string", "Size of original object, in centimeters, as entered in OpenPipe: width,height,depth. e.g.: \"111.8,87.9,1.0\""));
        properties.put("dimensions", this.array("float", "Size of original object, in centimeters: width,height,depth. e.g.: [111.8, 87.9, 1.0]"));
        properties.put("biography", this.prop("string", "Long text describing the asset."));
        properties.put("moment", this.prop("string", "If present, this asset will be a 'key moment' on a timeline, and given more focus or size."));

        properties.put("hasGeo", this.prop("boolean", "Does this asset have latitude/longitude positioning?"));
        properties.put("openpipe_latitude", this.prop("number", "WHEN hasGeo: (WGS 84 2D) latitude float"));
        properties.put("openpipe_longitude", this.prop("number", "WHEN hasGeo: (WGS 84 2D) longitude float"));

        properties.put("latlong", this.array("number", "WHEN hasGeo: (WGS 84 2D) an alternate representation of lat/long: [latitude, longitude]"));
        properties.put("geometry", this.prop("string", "WHEN in a template: OpenPipe layout position notation in pixels: \"width x height +- x-offset +- y-offset\" where + is from top/left and - is from bottom/right. e.g.: \"181.90 x 196.70 + 811.609 + 299.917\""));
        properties.put("size", this.array("string", "WHEN in a template: extracted from geometry - [width,height] in pixels. e.g.: [\"181.90\",\"196.70\"]"));
        properties.put("position", this.prop("string", "WHEN in a template: extracted from geometry - [x-offset,y-offset] in pixels. e.g.: e.g.: [\"+811.609\",\"+299.917\"]"));

        properties.put("openpipe_artist", this.array("string", "Names of artist topics for this asset"));
        properties.put("openpipe_nation", this.array("string", "Names of nation topics for this asset"));
        properties.put("openpipe_culture", this.array("string", "Names of culture topics for this asset"));
        properties.put("openpipe_genre", this.array("string", "Names of genre topics for this asset"));
        properties.put("openpipe_tags", this.array("string", "Names of tag topics for this asset"));
        properties.put("openpipe_classification", this.array("string", "Names of classification topics for this asset"));
        properties.put("openpipe_city", this.array("string", "Names of city topics for this asset"));
        properties.put("openpipe_medium", this.array("string", "Names of medium topics for this asset"));

        schema.put("properties", properties);
        schema.put("required", new JSONArray(new String[]{
            "title",
            "name",
            "date",
            "openpipe_date",
            "guid",
            "keyField",
            "keyRaw",
            "keyEncoded",
            "linkRelated",
            "type",
            "primaryImageSmall",
            "primaryImageFull",
            "primaryImageSmallDimensions",
            "primaryImageFullDimensions",
            "hasGeo"
        }));

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

    private JSONObject array(String type, String desc)
    {
        JSONObject x = new JSONObject();
        JSONObject a = new JSONObject();
        a.put("type", type);
        x.put("type", "array");
        x.put("description", desc);
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
