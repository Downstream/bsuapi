package bsuapi.dbal.query;

import bsuapi.dbal.NodeType;
import bsuapi.dbal.Topic;

public class TopicTop extends CypherQuery {
    /**
     * 1: Topic label
     * 2: label to use for rel count ":Topic"
     * 3: max # of matches
     */
    protected static String query =
        "MATCH p=("+ CypherQuery.resultColumn +":%1$s)-[]->(:%2$s) " +
        "WITH "+ CypherQuery.resultColumn +", count(p) as n " +
        "RETURN "+ CypherQuery.resultColumn +" ORDER BY n DESC " +
        "LIMIT %3$d; "
        ;

    public TopicTop(String query) {
        super(query);
    }

    public static TopicTop params (NodeType target)
    {
        TopicTop q = new TopicTop(TopicTop.query);

        q.target = target;
        q.args = new String[]{q.target.labelName(), NodeType.TOPIC.labelName()};
        q.resultQuery = String.format(q.initQuery, q.target.labelName(), NodeType.TOPIC.labelName(), q.limit);

        return q;
    }
}
