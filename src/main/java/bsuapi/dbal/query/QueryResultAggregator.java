package bsuapi.dbal.query;

import java.util.Map;

public interface QueryResultAggregator {
    void rowHandler(Map<String, Object> row);
    String getCommand();
}
