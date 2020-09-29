package bsuapi.dbal.query;

import bsuapi.obj.SettingGroup;

public class SettingsEntry extends CypherQuery
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
        "MATCH ("+ QueryResultSingleColumn.resultColumn +":%1$s {name: \"%2$s\"}) " +
        "RETURN "+ QueryResultSingleColumn.resultColumn +" LIMIT 1"
        ;

    private SettingGroup group;

    public SettingsEntry(SettingGroup group)
    {
        super(SettingsEntry.query);
        this.group = group;
    }

    public String getCommand()
    {
        return this.resultQuery = String.format(
            this.initQuery,
            this.group.label(),
            this.group.key()
        );
    }
}
