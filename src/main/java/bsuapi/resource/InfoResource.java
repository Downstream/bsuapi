package bsuapi.resource;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.InfoCards;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;


@Path("/info")
public class InfoResource extends BaseResource
{
    private static final int TIMEOUT = 1000;

    @Path("/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response apiAssets(
            @Context UriInfo uriInfo
    ){
        return this.apiAssets("Info", uriInfo);
    }

    @Path("/{card: [a-zA-Z]*}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response apiAssets(
        @PathParam("card") String card,
        @Context UriInfo uriInfo
    ){
        Response response = this.prepareResponse(uriInfo);
        JSONObject data = new JSONObject();

        try (
            Cypher c = new Cypher(db);
        ) {
            CypherQuery query = new InfoCards();

            JSONArray cards = query.exec(c);
            JSONObject infoSet = new JSONObject();
            infoSet.put("Info", cards);

            data.put("related", infoSet);
            data.put("node", this.findNode(card, cards) );
            data.put("topic", "Info");

            return response.data(data, InfoResource.nameString());
        } catch (Exception e) {
            return response.exception(e);
        }
    }

    private JSONObject findNode(String card, JSONArray cards)
    {
        for (Object jsNode : cards) {
            JSONObject entry;
            try { entry = (JSONObject) jsNode; } catch (ClassCastException e) {
                continue;
            }

            if (entry.optString("name").equals(card)) {
                return entry;
            }
        }

        return null;
    }

    private static String nameString()
    {
        return Config.getDefault("name", "Boise State World Museum Graph Archive") + " " + Config.getDefault("version", "0.1");
    }
}
