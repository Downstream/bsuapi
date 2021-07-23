package bsuapi.dbal.query;

import bsuapi.dbal.NodeType;

public class TopicTop extends CypherQuery
implements QueryResultSingleColumn
{
    /**
     * 1: Topic label
     * 2: label to use for rel count ":Topic"
     */
    protected static String query =
        "MATCH p=("+ QueryResultSingleColumn.resultColumn +":%1$s)-[]->(:%2$s) " +
        "WITH "+ QueryResultSingleColumn.resultColumn +", count(p) as n " +
        "%3$s RETURN "+ QueryResultSingleColumn.resultColumn +" ORDER BY n DESC "
        ;

    public TopicTop(NodeType target)
    {
        super(TopicTop.query);
        this.target = target;
    }

    public String getCommand()
    {
        return this.resultQuery = String.format(
            this.initQuery,
            this.target.labelName(),
            NodeType.TOPIC.labelName(),
            this.where(new String[]{QueryResultSingleColumn.resultColumn +".artCount > 1"})
        ) + this.getPageLimitCmd();
    }
}
