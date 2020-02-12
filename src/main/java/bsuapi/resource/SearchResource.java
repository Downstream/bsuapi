package bsuapi.resource;

import bsuapi.behavior.BehaviorType;
import bsuapi.behavior.Search;
import bsuapi.dbal.Cypher;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.IndexQuery;
import bsuapi.dbal.query.SearchPredictQuery;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/search")
public class SearchResource extends BaseResource
{
    private static final int TIMEOUT = 1000;

    protected Request request;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response search(
        @Context UriInfo uriInfo
    ){
        Response response = this.prepareSearchResponse(uriInfo);
        return response.notImplemented(Search.describe(), "Search form or UI not implemented.");
    }

    @Path("/{searchQuery}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response search(
            @PathParam("searchQuery") String searchQuery,
            @Context UriInfo uriInfo
    ){
        Response response = this.prepareSearchResponse(uriInfo);
        response.setSearch(URLCoder.decode(searchQuery));

        return this.handleBehavior(BehaviorType.SEARCH);
    }

    /* currently built to only check Topics */
    @Path("/predict/{searchQuery}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response predict(
            @PathParam("searchQuery") String searchQuery,
            @Context UriInfo uriInfo
    ){
        Response response = this.prepareSearchResponse(uriInfo);

        SearchPredictQuery query = new SearchPredictQuery("topicNameIndex", URLCoder.decode(searchQuery));
        query.setLimit(response.getParam(CypherQuery.limitParam));
        query.setPage(response.getParam(CypherQuery.pageParam));

        try (
                Cypher c = new Cypher(db)
        ) {
            JSONArray predictedMatches = query.exec(c);
            JSONObject data = new JSONObject();
            data.put("predictions", predictedMatches);
            data.put("query", query.toString());
            data.put("count", query.getResultCount());

            return response.data(data, "Completion Predictions");
        } catch (Exception e) {
            return response.exception(e);
        }
    }

    public Response prepareSearchResponse(UriInfo uriInfo)
    {
        this.request = new Request(uriInfo);
        return this.response = Response.prepare(this.request);
    }
}