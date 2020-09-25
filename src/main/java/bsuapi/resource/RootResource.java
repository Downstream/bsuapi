package bsuapi.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import bsuapi.behavior.Assets;
import bsuapi.behavior.Folder;
import bsuapi.behavior.Related;
import bsuapi.behavior.Search;
import bsuapi.dbal.*;
import bsuapi.dbal.query.*;
import bsuapi.obj.OpenPipeSettings;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.regex.Pattern;

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

        Cypher c = new Cypher(db);

        this.safeCypher(data, "topics", this::topicsList, c);
        this.safeCypher(data, "folders", this::folderList, c);
        this.safeCypher(data, "settings", this::settingsList, c);

        return response.plain(data);
    }

    private interface SafeCypherJSON { JSONObject apply(Cypher c) throws CypherException; }

    private void safeCypher(JSONObject data, String key, SafeCypherJSON dataLoader, Cypher c)
    {
        try {
            data.put(key, dataLoader.apply(c));
        } catch (Exception e) {
            data.put(key, new JSONObject());
            data.put(key +"-exception", this.exceptionHandler(e));
        }
    }

    private JSONObject buildMethodList()
    {
        JSONObject methods = new JSONObject();

        methods.put("root", this.youarehere());
        methods.put("related", Related.describe());
        methods.put("folder", Folder.describe());
        methods.put("topic-assets", Assets.describe());
        methods.put("search", Search.describe());
        methods.put("search/completion", Search.describeCompletion());
        methods.put("info", InfoResource.describe());
        methods.put("execute", ExecutorResource.describe());

        return methods;
    }

    private JSONObject buildParamList()
    {
        JSONObject params = new JSONObject();
        params.put(CypherQuery.limitParam, "(int) default:20 ignored:<1 - max number of results in a set :: /related will return 20 assets, and 20 of each topic-type.");
        params.put(CypherQuery.pageParam, "(int) default:1 ignored:<1 - paginated results according to limit, sets beyond the last page will be omitted :: limit=2&page=5 will return result entries 9 and 10 for each set (counting from 1).");
        params.put(CypherQuery.hasGeoParam, "(bool) default:false - show only results which have geospacial data (latlong) :: hasGeo=1.");
        params.put(Response.requestTokenParam, "(any urlencoded str) - will include the same token in the response body :: requestToken=abc123 => response: { \"requestToken\": \"abc123\"}");

        return params;
    }

    private JSONObject youarehere()
    {
        JSONObject data = new JSONObject();
        data.put("uri", "/");
        data.put("description", "API HOME - you are here - a place to help you get where you're going.");
        data.put("representation", "(THIS)");

        JSONObject params = new JSONObject();
        params.put("filter", "(optional) filter topics here by asset property. default \""+ Config.get("homeFilter") +"\"; example ?filter=period:Classical");
        data.put("parameters", params);
        return data;
    }

    private JSONObject topicsList(Cypher c)
    throws CypherException
    {
        // Collect and sanitize filter argument
        // @todo: can this be moved to become a global optional param? (affects every query, but may be possible)
        String[] filter = this.getParam("filter", Config.get("homeFilter")).split(":", 2);
        String filterField = null;
        String filterValue = null;
        if (filter.length > 1) {
            filterField = filter[0];
            filterValue = filter[1];

            Pattern strip = Pattern.compile("[^a-zA-Z0-9\\s]");
            filterField = strip.matcher(filterField).replaceAll("");
            filterValue = strip.matcher(filterValue).replaceAll("");

            if (filterValue == null || filterField.isEmpty() || filterValue.isEmpty()) {
                filterField = null;
            }
        }

        JSONObject topics = new JSONObject();
        for (NodeType n : NodeType.values()) {
            if (n.isTopic()) {
                CypherQuery query;
                if (filterField != null) {
                    query = new TopicTopFiltered(n, filterField, filterValue);
                } else {
                    query = new TopicTop(n);
                }

                query.setPage(this.getParam(CypherQuery.pageParam));
                query.setLimit(this.getParam(CypherQuery.limitParam));
                query.setHasGeo(Boolean.parseBoolean(this.getParam(CypherQuery.hasGeoParam)));
                JSONArray results = query.exec(c);
                topics.put(n.labelName(), results);
            }
        }
        return topics;
    }

    private JSONObject folderList(Cypher c)
    throws CypherException
    {
        CypherQuery query = new FolderList();
        query.setPage(this.getParam(CypherQuery.pageParam));
        query.setLimit(this.getParam(CypherQuery.limitParam));
        query.setHasGeo(Boolean.parseBoolean(this.getParam(CypherQuery.hasGeoParam)));
        JSONArray folderList = query.exec(c);

        JSONObject result = new JSONObject();
        result.put("folders", folderList);

        return result;
    }

    private JSONObject settingsList(Cypher c)
    throws CypherException
    {
        JSONObject result = new JSONObject();
        CypherQuery query = new SettingsList();
        result.put("query", query.getCommand());

        int i = 0;
        for(Object entry : query.exec(c)) {
            if (entry instanceof JSONObject) {
                OpenPipeSettings current = new OpenPipeSettings((JSONObject) entry);
                if (current.isValid()) {
                    result.put(current.name(), current.data());
                }
            }
        }

        result.put("config", this.getConfig());
        return result;
    }

    private JSONObject getConfig()
    {
        JSONObject result = new JSONObject();
        result.put("artifactId",Config.get("artifactId"));
        result.put("domain",Config.get("domain"));
        result.put("baseuri",Config.get("baseuri"));
        result.put("name",Config.get("name"));
        result.put("homeFilter",Config.get("homeFilter"));
        result.put("showErrors",Config.showErrors());
        return result;
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

    private JSONObject exceptionHandler(Exception e) {
        JSONObject exObj = new JSONObject();
        if (Config.showErrors() > 0) {
            exObj.put("message", e.getMessage());
            exObj.put("cause", e.getCause());
            exObj.put("stack", JsonResponse.exceptionStack(e));
        } else {
            exObj.put("message", e.getClass().getSimpleName());
        }
        return exObj;
    }
}
