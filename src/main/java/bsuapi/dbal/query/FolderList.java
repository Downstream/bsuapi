package bsuapi.dbal.query;

import bsuapi.dbal.NodeType;
import org.apache.commons.lang3.ArrayUtils;

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
        "MATCH (f:%1$s) " +
        "OPTIONAL MATCH (f)<-[:%2$s]-(x:Asset) " +
        "WITH count(x) AS cnt, f AS " + QueryResultSingleColumn.resultColumn + " " +
        "%3$s RETURN "+ QueryResultSingleColumn.resultColumn
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
            this.target.relFromAsset(),
            this.where()
        ) + this.getPageLimitCmd();
    }

    public void setTemplateOnly(boolean templateOnly) { this.templateOnly = templateOnly; }

    public String where() {
        String[] clauses = new String[]{" cnt > 0 "};

        if (this.templateOnly) {
            ArrayUtils.add(clauses, QueryResultSingleColumn.resultColumn +".hasLayout = true ");
        }

        return this.where(clauses);
    }
}
