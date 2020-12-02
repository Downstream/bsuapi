package bsuapi.dbal.query;

import bsuapi.dbal.NodeType;
import bsuapi.dbal.Topic;

public class FolderAssets extends CypherQuery
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
        "MATCH (%1$s)<-[r:%2$s]-("+ QueryResultSingleColumn.resultColumn +":%3$s) " +
        "%4$s RETURN "+ QueryResultSingleColumn.resultColumn +
            "{.*, type: head(labels("+ QueryResultSingleColumn.resultColumn +")), hasLayout: r.hasLayout, geometry: r.geometry, wall: r.wall, size: r.size, position: r.position} " +
        "ORDER BY "+ QueryResultSingleColumn.resultColumn +".score_generated DESC ,"+ QueryResultSingleColumn.resultColumn +".openpipe_id ASC "
        ;

    protected Topic topic;
    private boolean templateOnly = false;

    public FolderAssets(Topic topic)
    {
        super(FolderAssets.query);
        this.target = NodeType.ASSET;
        this.topic = topic;
    }

    public String getCommand()
    {
        return this.resultQuery = String.format(
            this.initQuery,
            this.topic.toCypherMatch(),
            this.topic.getType().relFromAsset(),
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
            result.append("EXISTS(r.geometry)");
        }

        if (result.length() > 0) {
            return " WHERE " + result.toString();
        }

        return "";
    }
}
