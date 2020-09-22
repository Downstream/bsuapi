package bsuapi.dbal.query;

import bsuapi.dbal.NodeType;
import bsuapi.dbal.Topic;

public class TopicSharedRelations extends CypherQuery
implements QueryResultSingleColumn
{
    protected boolean sameTopic;

    /**
     * 1: Topic label cypher match
     * 2: relation name (target)<-[:REL_NAME]-(topic)
     * 3: Target topic label
     * 4: max # of matches
     */
    protected static String query =
        "MATCH (a%1$s) " +
        "MATCH (a)-[r:%2$s]->("+ QueryResultSingleColumn.resultColumn +":%3$s) " +
        "MATCH p=(a)-[]->(:Topic)-[:%2$s]->("+ QueryResultSingleColumn.resultColumn +") " +
        "%4$s WITH a, "+ QueryResultSingleColumn.resultColumn +", count(p) as n " +
        "RETURN "+ QueryResultSingleColumn.resultColumn +", n " +
        "ORDER BY n DESC "
        ;

    protected static String querySameTopic =
        "MATCH (a%1$s) " +
        "MATCH p=(a)-[]->(:Topic)-[:%2$s]->("+ QueryResultSingleColumn.resultColumn +":%3$s) " +
        "%4$s WITH a, "+ QueryResultSingleColumn.resultColumn +", count(p) as n " +
        "RETURN "+ QueryResultSingleColumn.resultColumn +", n " +
        "ORDER BY n DESC "
        ;

    protected Topic topic;

    public TopicSharedRelations(Topic topic, NodeType target)
    {
        super(TopicSharedRelations.query);
        this.sameTopic = topic.name().equals(target.labelName());

        if (this.sameTopic) {
            this.initQuery = TopicSharedRelations.querySameTopic;
            this.resultQuery = TopicSharedRelations.querySameTopic;
        }

        this.topic = topic;
        this.target = target;
    }

    public String getCommand()
    {
        return this.resultQuery = String.format(
            this.initQuery,
            this.topic.toCypherMatch(),
            this.target.relFromTopic(),
            this.target.labelName(),
            this.getWhere()
        ) + this.getPageLimitCmd();
    }

    public String getWhere()
    {
        if (this.sameTopic) {
            return this.where(new String[]{QueryResultSingleColumn.resultColumn +" <> a "});
        }

        return this.where();
    }
}
