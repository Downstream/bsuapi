package bsuapi.dbal.script;

public enum CypherScript
{
    INFO,
    OPENPIPE_REBUILD,
    OPENPIPE_TOPICIMG,
    OPENPIPE_SYNC;

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
}
