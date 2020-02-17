package bsuapi.behavior;

import bsuapi.dbal.query.IndexQuery;

import java.util.Map;

public class TopicIndex extends IndexBehaviorBase
{
    public static final String indexName = "topicNameIndex";

    public TopicIndex(Map<String, String> config)
    throws BehaviorException
    {
        super(config);
    }

    @Override
    public String resultKey() {
        return "topic-results";
    }

    @Override
    public IndexQuery createQuery(String value) {
        return new IndexQuery(TopicIndex.indexName, value);
    }
}
