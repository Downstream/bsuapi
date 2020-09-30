package bsuapi.dbal.query;

import bsuapi.dbal.NodeType;

public class FolderList extends CypherQuery
implements QueryResultSingleColumn
{
    /**
     * See /src/resources/openPipe-folders.cypher
     * 1: Topic label cypher match
     * 2: relation name (topic)<-[:REL]-(asset)
     * 3: Asset label "Asset"
     * 4: max # of matches
     */
    protected static String query =
        "MATCH ("+ QueryResultSingleColumn.resultColumn +":%1$s) " +
        "%2$s RETURN "+ QueryResultSingleColumn.resultColumn
        ;

    private boolean templateOnly = false;
    public static final String templateOnlyParam = "hasLayout";

    public FolderList()
    {
        super(FolderList.query);
        this.target = NodeType.FOLDER;
    }

    public String getCommand()
    {
        return this.resultQuery = String.format(
            this.initQuery,
            this.target.labelName(),
            this.where()
        ) + this.getPageLimitCmd();
    }

    public void setTemplateOnly(boolean templateOnly) { this.templateOnly = templateOnly; }

    public String where() {
        StringBuilder result = new StringBuilder();
        if (this.hasGeo) {
            result.append(QueryResultSingleColumn.resultColumn +".hasGeo = true");
        }

        if (this.templateOnly) {
            result.append(QueryResultSingleColumn.resultColumn +".hasLayout = true");
        }

        if (result.length() > 0) {
            return " WHERE " + result.toString();
        }

        return "";
    }
}
