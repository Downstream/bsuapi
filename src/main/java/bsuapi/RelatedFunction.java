package bsuapi;

import bsuapi.behavior.Related;
import bsuapi.dbal.Topic;
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

            Related rel = new Related(t);
            return rel.node; // @todo: return a list of properties matching the resource response (Neo4j doesn't handle JSONObject)
        }
    }
}
