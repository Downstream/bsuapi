package bsuapi.resource;

import bsuapi.behavior.BehaviorType;
import bsuapi.behavior.Search;
import bsuapi.dbal.Cypher;

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

    @Path("/{query}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response search(
            @PathParam("query") String query,
            @Context UriInfo uriInfo
    ){
        Response response = this.prepareSearchResponse(uriInfo);
        response.setSearch(URLCoder.decode(query));

        try (
                Cypher c = new Cypher(db)
        ) {
            return response.behavior(BehaviorType.SEARCH, c);
        }
        catch (Exception e)
        {
            Util.logException(log, e, "Search Error");
            return response.exception(e);
        }
    }

    public Response prepareSearchResponse(UriInfo uriInfo)
    {
        this.request = new Request(uriInfo);
        return this.response = Response.prepare(this.request);
    }

}