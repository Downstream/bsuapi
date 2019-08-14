package bsuapi;

import bsuapi.dbal.Topic;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import java.util.ArrayList;
import java.util.List;

public class RelatedFunction
{
    @Context
    public GraphDatabaseService db;

    @UserFunction
    @Description("bsuapi.related('Topic','IndexedValue') - find all Topics with a matching indexed value, with related Topics and Artwork. API /related/{Topic}/{Value}")
    public List<Node> related(
            @Name("labelName") String labelName,
            @Name("value") String value
    ){
        ArrayList<Node> matches;

        try ( Transaction tx = this.db.beginTx() ) {
            Schema schema = this.db.schema();
            Topic l = new Topic(db, labelName);
            matches = new ArrayList<>(l.findRelated(value));

            tx.success();
        }

        return matches;
    }
}
