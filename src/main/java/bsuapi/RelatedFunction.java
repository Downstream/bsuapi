package bsuapi;

import bsuapi.behavior.BehaviorException;
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
            @Name("topicLabel") String topicLabel,
            @Name("topicKey") String topicKey
    ) throws CypherException
    {
        try (
                Cypher c = new Cypher(db)
        ) {

            Related rel = new Related(Topic.plainMap(topicLabel, topicKey));
            rel.resolveBehavior(c);

            return rel.getNeoNode(); // @todo: return a list of properties matching the resource response (Neo4j doesn't handle JSONObject)
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
