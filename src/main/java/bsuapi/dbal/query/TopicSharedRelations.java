package bsuapi.dbal.query;

import bsuapi.dbal.NodeType;
import bsuapi.dbal.Topic;

import java.util.ArrayList;

public class TopicSharedRelations extends CypherQuery {
    /**
     * 1: Topic label
     * 2: Target topic label
     * 3: relation name (target)<-[:REL_NAME]-(topic)
     * 4: max # of matches
     */
    protected static String query =
        "MATCH (a:%1$s)" +
        "MATCH (a)-[r:%3$s]->(t:%2$s)" +
        "MATCH p=(a)-[]->(:Topic)-[:%3$s]->(t)" +
        "WITH a, t, count(p) as n" +
        "RETURN a, t, n" +
        "ORDER BY n DESC" +
        "LIMIT %4$d;"
        ;

    protected int limit = 10;
    protected Topic topic;
    protected NodeType target;

    public TopicSharedRelations(String query) {
        super(query);
    }

    public static TopicSharedRelations params (Topic topic, NodeType target)
    {
        TopicSharedRelations q = new TopicSharedRelations(TopicSharedRelations.query);
        q.args = new String[]{topic.name(), target.relFromTopic(), target.labelName()};
        q.topic = topic;
        q.target = target;
        q.resultQuery = String.format(q.initQuery, topic.name(), target.relFromTopic(), target.labelName(), q.limit);

        return q;
    }
}
