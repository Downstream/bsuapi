package bsuapi.resource;

import bsuapi.behavior.Assets;
import bsuapi.behavior.Related;
import bsuapi.behavior.SearchBehavior;
import bsuapi.dbal.Cypher;
import bsuapi.dbal.query.Search;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.graphdb.Transaction;

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
        return response.notImplemented(SearchBehavior.describe(), "Search form or UI not implemented.");
    }

    @Path("/{query}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response search(
            @PathParam("query") String query,
            @Context UriInfo uriInfo
    ){
        Response response = this.prepareSearchResponse(uriInfo);

        try (
                Cypher c = new Cypher(db);
                Transaction tx = db.beginTx();
        ) {
            // prepare
            Search search = new Search(URLCoder.decode(query));
            SearchBehavior b = new SearchBehavior(search);

            // compose
            b.setConfig(this.request.getQueryParameters()); // querystring params sanitized into behavior params
            b.setQueryConfig(search); // pulls preset behavior params into CypherQuery

            // resolve
            b.resolveBehavior(c);
            b.addToLog(log);

            tx.success();

            if (b.length() <= 0) {
                return response.noContent(b.getMessage());
            }

            return response.data(b.toJson(), b.getMessage());
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