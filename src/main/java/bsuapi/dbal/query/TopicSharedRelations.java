package bsuapi.dbal.query;

import bsuapi.dbal.NodeType;
import bsuapi.dbal.Topic;

public class TopicSharedRelations extends CypherQuery {
    /**
     * 1: Topic label cypher match
     * 2: relation name (target)<-[:REL_NAME]-(topic)
     * 3: Target topic label
     * 4: max # of matches
     */
    protected static String query =
        "MATCH (a%1$s) " +
        "MATCH (a)-[r:%2$s]->("+ CypherQuery.resultColumn +":%3$s) " +
        "MATCH p=(a)-[]->(:Topic)-[:%2$s]->("+ CypherQuery.resultColumn +") " +
        "WITH a, "+ CypherQuery.resultColumn +", count(p) as n " +
        "RETURN "+ CypherQuery.resultColumn +", n " +
        "ORDER BY n DESC " +
        "LIMIT %4$d; "
        ;

    protected static String querySameTopic =
        "MATCH (a%1$s) " +
        "MATCH p=(a)-[]->(:Topic)-[:%2$s]->("+ CypherQuery.resultColumn +":%3$s) " +
        "WHERE "+ CypherQuery.resultColumn +" <> a " +
        "WITH a, "+ CypherQuery.resultColumn +", count(p) as n " +
        "RETURN "+ CypherQuery.resultColumn +", n " +
        "ORDER BY n DESC " +
        "LIMIT %4$d; "
        ;

    protected Topic topic;

    public TopicSharedRelations(String query) {
        super(query);
    }

    public static TopicSharedRelations params (Topic topic, NodeType target)
    {
        TopicSharedRelations q;
        if (topic.name().equals(target.labelName())) {
            q = new TopicSharedRelations(TopicSharedRelations.querySameTopic);
        } else {
            q = new TopicSharedRelations(TopicSharedRelations.query);
        }

        q.args = new String[]{topic.toCypherMatch(), target.relFromTopic(), target.labelName()};
        q.topic = topic;
        q.target = target;
        q.resultQuery = String.format(q.initQuery, topic.toCypherMatch(), target.relFromTopic(), target.labelName(), q.limit);

        return q;
    }
}
