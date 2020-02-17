package bsuapi.dbal.query;

public interface QueryResultSingleColumn extends QueryResultCollector {
    public static final String resultColumn = "t";
    void entryHandler(String entry);
    void entryHandler(Object entry);
    String getCommand();
}
