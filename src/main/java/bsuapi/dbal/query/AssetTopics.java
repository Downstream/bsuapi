package bsuapi.dbal.query;

import bsuapi.dbal.Asset;
import bsuapi.dbal.NodeType;

public class AssetTopics extends CypherQuery
implements QueryResultSingleColumn
{
    protected static String query =
        "MATCH (%1$s)-[r:%2$s]->("+ QueryResultSingleColumn.resultColumn +":%3$s) " +
        "%4$s " +
        "RETURN "+ QueryResultSingleColumn.resultColumn +" ORDER BY "+ QueryResultSingleColumn.resultColumn +".artCount DESC"
        ;


    protected Asset asset;

    public AssetTopics(Asset asset, NodeType target)
    {
        super(AssetTopics.query);
        this.asset = asset;
        this.target = target;
    }

    public String getCommand()
    {
        return this.resultQuery = String.format(
            this.initQuery,
            this.asset.toCypherMatch(),
            this.target.relFromAsset(),
            this.target.labelName(),
            this.where(new String[]{ QueryResultSingleColumn.resultColumn +".artCount > 1" })
        ) + this.getPageLimitCmd();
    }
}
