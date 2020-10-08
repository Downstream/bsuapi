package bsuapi.obj;

import bsuapi.dbal.NodeType;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.SettingsEntry;
import bsuapi.dbal.query.SettingsList;
import org.json.JSONObject;

public enum SettingGroup
{
    GLOBE,
    TIMELINE,
    EXPLORE,
    COLOR,
    CONNECTION;

    public String key()
    {
        return this.name().toLowerCase();
    }

    public String label() { return NodeType.OPEN_PIPE_SETTING.labelName(); }

    public CypherQuery query()
    {
        switch (this) {
            case COLOR:
                return new SettingsEntry(this);
            case GLOBE:
                CypherQuery q = new SettingsEntry(this);
                q.setHasGeo(true);
                return q;
            case TIMELINE:
            case EXPLORE:
            case CONNECTION:
            default:
                return new SettingsList(this);
        }
    }

    public static SettingGroup[] active()
    {
        return new SettingGroup[]{
            GLOBE,
            TIMELINE,
            EXPLORE,
            COLOR,
            CONNECTION
        };
    }

    public static SettingGroup match(String name)
    throws IllegalArgumentException
    {
        // note: Java bug in valueOf? if the entry is not found, valueOf neither completes nor throws an exception
        for ( SettingGroup n : SettingGroup.values() ) {
            if ( n.toString().equalsIgnoreCase(name) ) {
                return n;
            }
        }

        throw new IllegalArgumentException(name +" is not a valid SettingGroup.");
    }

    public String description()
    {
        switch(this) {
            case GLOBE: return "Preconfigured options (Topics and Folders) as the entry point for Globe mode.";
            case TIMELINE: return "Preconfigured options (Topics and Folders) as the entry point for the Timeline. May additionally be focused by TopicType (option.byTopic: type).";
            case EXPLORE: return "Preconfigured options (Topics and Folders) as the entry point for Explore mode.";
            case COLOR: return "Preconfigured color-pairs for Color mode.";
            case CONNECTION: return "Preconfigured options (Topics and Folders) as the entry point for Connections mode.";
        }

        return "unknown";
    }

    public static JSONObject describeAll()
    {
        JSONObject desc = new JSONObject();
        for (SettingGroup g : SettingGroup.active()) {
            desc.put(g.key(), g.description());
        }
        return desc;
    }
}
