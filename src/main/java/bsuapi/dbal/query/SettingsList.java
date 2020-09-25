package bsuapi.dbal.query;

import bsuapi.dbal.NodeType;

public class SettingsList extends CypherQuery
implements QueryResultSingleColumn
{
    protected static String SETTINGS_LABEL = "OpenPipeSetting";

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

    public SettingsList()
    {
        super(SettingsList.query);
    }

    public String getCommand()
    {
        return this.resultQuery = String.format(
            this.initQuery,
            SETTINGS_LABEL
        );
    }
}
