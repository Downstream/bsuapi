package bsuapi.settings;

import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.QueryResultSingleColumn;

public class SettingsTopicsOnly extends CypherQuery
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
        "MATCH (g:%1$s {name: \"%2$s\"})<-[rel:SETTING_OPTION]-("+ QueryResultSingleColumn.resultColumn +":Topic) " +
        "%3$s RETURN "+ QueryResultSingleColumn.resultColumn
        ;

    private SettingGroup group;

    public SettingsTopicsOnly(SettingGroup group)
    {
        super(SettingsTopicsOnly.query);
        this.group = group;
    }

    public String getCommand()
    {
        return this.resultQuery = String.format(
            this.initQuery,
            this.group.label(),
            this.group.key(),
            this.where()
        );
    }
}
