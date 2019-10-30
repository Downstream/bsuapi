package bsuapi.resource;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.Node;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.CypherScriptFile;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;


@Path("/search")
public class SearchResource extends BaseResource
{
    private static final int TIMEOUT = 1000;

    @Path("/{query}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response search(
            @Context UriInfo uriInfo,
            @PathParam("query") String query
    ){
        Response response = this.prepareResponse(uriInfo);

        try (
                Cypher c = new Cypher(db);
        ) {
            JSONObject data = new JSONObject();
            JSONArray results = this.runQuery(c, this.cleanCommand(query));
            data.put("results",results);

            return response.data(data, "search results for: "+query);
        }
        catch (Exception e)
        {
            return response.exception(e);
        }
    }

    private String cleanCommand(String query)
    {
        // @todo sanitize for lucene
        // @todo add to RootResource & document search-syntax
        // lucene.apache.org/core/5_5_0/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#package.description
        // neo4j.com/docs/cypher-manual/3.5/schema/index/#schema-index-fulltext-search
        // neo4j.com/developer/kb/fulltext-search-in-neo4j
        return "CALL db.index.fulltext.queryNodes(\"nameIndex\", \""+query+"\")";
    }

    private JSONArray runQuery(Cypher c, String command)
    throws CypherException
    {
        try (
                Transaction tx = db.beginTx();
                Result r = c.execute(command)
        ) {
            JSONArray data = new JSONArray();
            while ( r.hasNext()) {
                Map<String,Object> row = r.next();
                JSONObject node = null;
                double score = 0;
                for ( Map.Entry<String,Object> column : row.entrySet() ) {
                    Object value = column.getValue();
                    if (column.getKey().equals("node") && value instanceof org.neo4j.graphdb.Node) {
                        node = (new Node((org.neo4j.graphdb.Node) value)).toJsonObject();
                    } else if (column.getKey().equals("score")) {
                        try {
                            score = (double) value;
                        } catch (ClassCastException ignored) {
                            score = 0;
                        }
                    }
                }

                if (null != node) {
                    node.put("searchScore", score);
                    data.put(node);
                }
            }
            tx.success();
            return data;

        } catch (Exception e) {
            throw new CypherException("Cypher full-text-index query failed: "+command, e);
        }
    }
}