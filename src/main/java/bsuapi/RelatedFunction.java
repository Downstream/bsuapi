package bsuapi;

import bsuapi.dbal.Topic;
import org.json.JSONObject;
import org.neo4j.graphdb.*;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

public class RelatedFunction
{
    @Context
    public GraphDatabaseService db;

    @UserFunction
    @Description("bsuapi.related('Topic','IndexedValue') - find all Topics with a matching indexed value, with related Topics and Artwork. API /related/{Topic}/{Value}")
    public Node related(
            @Name("labelName") String labelName,
            @Name("value") String value
    ){
        try ( Transaction tx = this.db.beginTx() )
        {
            Topic t = new Topic(db, labelName, value);
            tx.success();

            JSONObject data = new JSONObject();
            data.put("topic", t.name());
            data.put("node", t.toJson());
            data.put("name", t.getNodeName());
            data.put("nearby", t.altsJson());
            return t.getNode();
        }
    }
}
