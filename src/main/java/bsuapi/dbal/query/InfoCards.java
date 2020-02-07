package bsuapi.dbal.query;

public class InfoCards extends CypherQuery
{
    /**
     * 1: Topic label
     * 2: label to use for rel count ":Topic"
     * 3: max # of matches
     */
    protected static String query = "MATCH ("+CypherQuery.resultColumn+":Info) RETURN "+CypherQuery.resultColumn;

    public InfoCards() {
        super(InfoCards.query);
    }

    public String getCommand() {
        return this.resultQuery = this.initQuery;
    }
}
