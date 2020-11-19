package bsuapi.dbal.query;

import bsuapi.dbal.Asset;
import bsuapi.dbal.NodeType;
import bsuapi.dbal.Topic;

public class AssetTopics extends CypherQuery
implements QueryResultSingleColumn
{
    protected static String query =
        "MATCH (%1$s)-[r:%2$s]->("+ QueryResultSingleColumn.resultColumn +":%3$s) " +
        "%4$s WITH "+ QueryResultSingleColumn.resultColumn +", count("+ QueryResultSingleColumn.resultColumn +") as n" +
        "RETURN "+ QueryResultSingleColumn.resultColumn +" ORDER BY n DESC"
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
            this.where()
        ) + this.getPageLimitCmd();
    }
}
