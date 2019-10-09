package bsuapi.dbal.query;

import bsuapi.dbal.NodeType;
import bsuapi.dbal.Topic;
import org.neo4j.cypher.internal.compiler.v2_3.No;

public class TopicAssets extends CypherQuery {
    /**
     * 1: Topic label cypher match
     * 2: relation name (topic)<-[:REL]-(asset)
     * 3: Asset label "Artwork"
     * 4: max # of matches
     */
    protected static String query =
        "MATCH (%1$s)<-[:%2$s]-("+ CypherQuery.resultColumn +":%3$s) " +
        "RETURN "+ CypherQuery.resultColumn +" " +
        "ORDER BY "+ CypherQuery.resultColumn +".objectID ASC " +
        "LIMIT %4$d; "
        ;

    protected Topic topic;
    protected NodeType target = NodeType.ARTWORK;

    public TopicAssets(String query) {
        super(query);
    }

    public static TopicAssets params (Topic topic)
    {
        TopicAssets q = new TopicAssets(TopicAssets.query);
        String rel = topic.getType().relFromAsset();

        q.args = new String[]{topic.toCypherMatch(), rel, q.target.labelName()};
        q.topic = topic;
        q.resultQuery = String.format(q.initQuery, topic.toCypherMatch(), rel, q.target.labelName(), q.limit);

        return q;
    }
}
