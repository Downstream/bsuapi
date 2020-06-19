package bsuapi.dbal.script;

import bsuapi.dbal.Cypher;
import org.json.JSONObject;

public enum CypherScript
{
    INFO,
    OPENPIPE_REBUILD,
    OPENPIPE_TOPICIMG,
    OPENPIPE_SYNC;

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
            case OPENPIPE_REBUILD:
                return "openPipe-rebuild.cypher";
            case OPENPIPE_SYNC:
                return "openPipe-sync.cypher";
            case OPENPIPE_TOPICIMG:
                return "openPipe-topicImg.cypher";
            default:
                throw new IllegalStateException( "Unknown CypherScript " + this );
        }
    }

    public int type()
    {
        switch ( this )
        {
            case INFO:
            case OPENPIPE_REBUILD:
            case OPENPIPE_SYNC:
            case OPENPIPE_TOPICIMG:
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
}
