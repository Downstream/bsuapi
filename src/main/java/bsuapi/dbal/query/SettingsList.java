package bsuapi.dbal.query;

import bsuapi.obj.SettingGroup;

public class SettingsList extends CypherQuery
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
        "MATCH (g:%1$s {name: \"%2$s\"})<-[rel:SETTING_OPTION]-(a) " +
        "RETURN CASE WHEN rel.byType IS NOT NULL THEN a{.*, type: head(labels(a)), byType: rel.byType} ELSE a END AS "+ QueryResultSingleColumn.resultColumn
        ;

    private SettingGroup group;

    public SettingsList(SettingGroup group)
    {
        super(SettingsList.query);
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
