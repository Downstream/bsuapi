package bsuapi.dbal.query;

import bsuapi.dbal.NodeType;
import bsuapi.dbal.Topic;

public class AssetTopics extends CypherQuery
implements QueryResultSingleColumn
{
    /**
     * See /src/resources/openPipe-folders.cypher
     * 1: Topic label cypher match
     * 2: relation name (topic)<-[:REL]-(asset)
     * 3: Asset label "Asset"
     * 4: max # of matches
     */
    protected static String query =
        "MATCH (%1$s)<-[r:%2$s]-("+ QueryResultSingleColumn.resultColumn +":%3$s) " +
        "%4$s RETURN "+ QueryResultSingleColumn.resultColumn +
            "{.*, type: head(labels("+ QueryResultSingleColumn.resultColumn +")), hasLayout: r.hasLayout, geometry: r.geometry, wall: r.wall, size: r.size, position: r.position} " +
        "ORDER BY "+ QueryResultSingleColumn.resultColumn +".score_generated DESC ,"+ QueryResultSingleColumn.resultColumn +".openpipe_id ASC "
        ;

    /**
     * 1: Topic label cypher match
     * 2: relation name (target)<-[:REL_NAME]-(topic)
     * 3: Target topic label
     * 4: max # of matches
     */
    protected static String query2 =
            "MATCH (a%1$s) " +
                    "MATCH (a)-[r:%2$s]->("+ QueryResultSingleColumn.resultColumn +":%3$s) " +
                    "MATCH p=(a)-[]->(:Topic)-[:%2$s]->("+ QueryResultSingleColumn.resultColumn +") " +
                    "%4$s WITH a, "+ QueryResultSingleColumn.resultColumn +", count(p) as n " +
                    "RETURN "+ QueryResultSingleColumn.resultColumn +", n " +
                    "ORDER BY n DESC "
            ;


    protected Topic topic;

    public AssetTopics(NodeType target)
    {
        super(AssetTopics.query);
        this.target = NodeType.ASSET;
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
