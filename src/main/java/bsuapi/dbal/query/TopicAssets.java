package bsuapi.dbal.query;

import bsuapi.dbal.NodeType;
import bsuapi.dbal.Topic;

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

    public TopicAssets(Topic topic) {
        super(TopicAssets.query);
        this.target = NodeType.ARTWORK;
        this.topic = topic;
    }

    public String getCommand()
    {
        return this.resultQuery = String.format(
            this.initQuery,
            this.topic.toCypherMatch(),
            this.topic.getType().relFromAsset(),
            this.target.labelName(),
            this.limit
        );
    }
}
