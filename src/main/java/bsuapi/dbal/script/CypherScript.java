package bsuapi.dbal.script;

import bsuapi.dbal.Cypher;
import org.json.JSONObject;

public enum CypherScript
{
    INFO,
    OPENPIPE_RESET,
    OPENPIPE_TOPICIMG,
    OPENPIPE_SYNC,
    OPENPIPE_FOLDERS,
    OPENPIPE_SETTINGS;

    static final int TYPE_FILE_INCLUDED = 1;  // included in the jar
    static final int TYPE_FILESYSTEM = 2;     // accessible in the filesystem
    static final int TYPE_DB = 3;             // stored as a string in a node

    public String filename()
    throws IllegalStateException
    {
        switch ( this )
        {
            case INFO:
                return "infoCards.cypher";
            case OPENPIPE_RESET:
                return "openPipe-reset.cypher";
            case OPENPIPE_SYNC:
                return "openPipe-sync.cypher";
            case OPENPIPE_TOPICIMG:
                return "openPipe-topicImage.cypher";
            case OPENPIPE_FOLDERS:
                return "openPipe-folders.cypher";
            case OPENPIPE_SETTINGS:
                return "openPipe-settings.cypher";
        }

        throw new IllegalStateException( "Unknown CypherScript " + this );
    }

    public int type()
    {
        switch ( this )
        {
            case INFO:
            case OPENPIPE_RESET:
            case OPENPIPE_SYNC:
            case OPENPIPE_TOPICIMG:
            case OPENPIPE_FOLDERS:
            case OPENPIPE_SETTINGS:
                return TYPE_FILE_INCLUDED;
            default:
                return 0;
        }
    }

    public CypherScriptAbstract getRunner(Cypher c)
    {
        switch (this.type()) {
            case TYPE_FILE_INCLUDED:
                return CypherScriptFile.instance(c, this);
            case TYPE_FILESYSTEM:
                throw new UnsupportedOperationException("Filesystem scripts are not yet supported due to security implications.");
            case TYPE_DB:
                throw new UnsupportedOperationException("Scripts stored in db are not yet supported due to security implications.");
            default:
                throw new IllegalStateException( "Unknown CypherScript " + this );
        }
    }

    public JSONObject getStoredReport(Cypher c)
    {
        return CypherScriptAbstract.getStoredStatus(c, this);
    }

    public String describe()
    {
        switch ( this )
        {
            case INFO:
                return "Regenerate :Info nodes, predefined in script.";
            case OPENPIPE_RESET:
                return "Clear Assets and Topics, and re-import all from OpenPipe.";
            case OPENPIPE_SYNC:
                return "Retrieve Assets modified since last sync, and adjust the graph.";
            case OPENPIPE_TOPICIMG:
                return "Reassess Assets selected as representative of Topics.";
            case OPENPIPE_FOLDERS:
                return "Retrieve Folders and Templates, and updating those Folders' list of Assets.";
            case OPENPIPE_SETTINGS:
                return "Retrieve Settings from OpenPipe, and add to :OpenPipeSettings in db.";
            default:
                throw new IllegalStateException( "Unknown CypherScript " + this );
        }
    }

    public static JSONObject describeAll()
    {
        JSONObject result = new JSONObject();
        for(CypherScript script : CypherScript.values()) {
            result.put(script.name(), script.describe());
        }
        return result;
    }
}
