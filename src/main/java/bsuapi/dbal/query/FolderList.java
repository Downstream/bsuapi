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
        "RETURN "+ QueryResultSingleColumn.resultColumn
        ;

    public FolderList()
    {
        super(FolderList.query);
        this.target = NodeType.FOLDER;
    }

    public String getCommand()
    {
        return this.resultQuery = String.format(
            this.initQuery,
            this.target.labelName()
        ) + this.getPageLimitCmd();
    }
}
