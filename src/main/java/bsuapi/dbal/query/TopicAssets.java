package bsuapi.dbal.query;

import bsuapi.dbal.NodeType;
import bsuapi.dbal.Topic;

public class TopicAssets extends CypherQuery
implements QueryResultSingleColumn
{
    /**
     * 1: Topic label cypher match
     * 2: relation name (topic)<-[:REL]-(asset)
     * 3: Asset label "Asset"
     * 4: max # of matches
     */
    protected static String query =
        "MATCH (%1$s)<-[:%2$s]-("+ QueryResultSingleColumn.resultColumn +":%3$s) " +
        "%4$s RETURN "+ QueryResultSingleColumn.resultColumn +" " +
        "ORDER BY "+ QueryResultSingleColumn.resultColumn +".score_generated DESC ,"+ QueryResultSingleColumn.resultColumn +".openpipe_id ASC "
        ;

    protected Topic topic;

    public TopicAssets(Topic topic)
    {
        super(TopicAssets.query);
        this.target = NodeType.ASSET;
        this.topic = topic;
    }

    public String getCommand()
    {
        return this.resultQuery = String.format(
            this.initQuery,
            this.topic.toCypherMatch(),
            this.topic.getType().relFromAsset(),
            this.target.labelName(),
            this.where()
        ) + this.getPageLimitCmd();
    }
}
