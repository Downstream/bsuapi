package bsuapi.dbal.query;

import bsuapi.dbal.NodeType;

public class TopicTop extends CypherQuery {
    /**
     * 1: Topic label
     * 2: label to use for rel count ":Topic"
     */
    protected static String query =
        "MATCH p=("+ CypherQuery.resultColumn +":%1$s)-[]->(:%2$s) " +
        "WITH "+ CypherQuery.resultColumn +", count(p) as n " +
        "RETURN "+ CypherQuery.resultColumn +" ORDER BY n DESC "
        ;

    public TopicTop(NodeType target) {
        super(TopicTop.query);
        this.target = target;
    }

    public String getCommand()
    {
        return this.resultQuery = String.format(
            this.initQuery,
            this.target.labelName(),
            NodeType.TOPIC.labelName()
        ) + this.getPageLimitCmd();
    }
}
