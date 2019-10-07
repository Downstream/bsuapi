package bsuapi;

import bsuapi.behavior.Related;
import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
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
    @Description("bsuapi.related('Topic','IndexedValue') - find all Topics with a matching indexed value, with related Topics and Assets. API /related/{Topic}/{Value}")
    public Node related(
            @Name("labelName") String labelName,
            @Name("value") String value
    ) throws CypherException
    {
        try (
                Cypher c = new Cypher(db);
        ) {
            Topic t = new Topic(labelName, value);
            c.resolveTopic(t);
            Related rel = new Related(t);
            rel.resolveBehavior(c);

            return rel.node; // @todo: return a list of properties matching the resource response (Neo4j doesn't handle JSONObject)
        }
    }
}
