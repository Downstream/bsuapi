package bsuapi.dbal;
import org.neo4j.graphdb.Label;

public enum NodeType {
    ARTWORK,
    ARTIST,
    CLASS,
    CULTURE,
    NATION,
    TAG,
    TOPIC;

    public Label label()
    {
        return org.neo4j.graphdb.Label.label(this.labelName());
    }

    public String labelName()
    {
        switch ( this )
        {
            case ARTWORK:
                return "Artwork";
            case ARTIST:
                return "Artist";
            case CLASS:
                return "Classification";
            case CULTURE:
                return "Culture";
            case NATION:
                return "Nation";
            case TAG:
                return "Tag";
            case TOPIC:
                return "Topic";
            default:
                throw new IllegalStateException( "Unknown Node Type enum: " + this );
        }
    }

    public String relFromTopic()
    {
        switch ( this )
        {
            case ARTIST:
                return "ARTIST";
            case CLASS:
                return "CLASS";
            case CULTURE:
                return "CULTURE";
            case NATION:
                return "NATION";
            case TAG:
                return "TAG";
            case TOPIC:
                throw new IllegalArgumentException( "The generic 'Topic' label matches all Topics, thus has no specific named relationship.");
            case ARTWORK:
                throw new IllegalArgumentException( "Assets do not have relationships from Topics");
            default:
                throw new IllegalStateException( "Unknown Node Type enum: " + this );
        }
    }

    public static NodeType match(String label)
    {
        try {
            return NodeType.valueOf(label);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(label +" is not a valid NodeType.", e);
        }
    }

    public Boolean isTopic()
    {
        switch ( this )
        {
            case ARTIST:
            case CLASS:
            case CULTURE:
            case NATION:
            case TAG:
                return true;
            default:
                return false;
        }
    }
}
