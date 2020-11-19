package bsuapi.dbal;
import bsuapi.resource.Config;
import bsuapi.resource.URLCoder;
import org.neo4j.graphdb.Label;

public enum NodeType
{
    ASSET,
    ARTIST,
    CLASS,
    CULTURE,
    NATION,
    TAG,
    GENRE,
    MEDIUM,
    CITY,
    TOPIC,
    FOLDER,
    OPEN_PIPE_SETTING;

    public Label label()
    {
        return org.neo4j.graphdb.Label.label(this.labelName());
    }

    public String labelName()
    throws IllegalStateException
    {
        switch ( this )
        {
            case ASSET:
                return "Asset";
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
            case GENRE:
                return "Genre";
            case MEDIUM:
                return "Medium";
            case CITY:
                return "City";
            case FOLDER:
                return "Folder";
            case OPEN_PIPE_SETTING:
                return "OpenPipeSetting";
            default:
                throw new IllegalStateException( "Unknown Node Type enum: " + this );
        }
    }

    public String relFromTopic()
    throws IllegalArgumentException, IllegalStateException
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
            case GENRE:
                return "GENRE";
            case MEDIUM:
                return "MEDIUM";
            case CITY:
                return "CITY";
            case FOLDER:
                return "FOLDER_TOPIC";
            case OPEN_PIPE_SETTING:
                return "SETTING_OPTION";
            case TOPIC:
                throw new IllegalArgumentException( "The generic 'Topic' label matches all Topics, thus has no specific named relationship.");
            case ASSET:
                throw new IllegalArgumentException( "Assets do not have relationships from Topics. e.g.: (:Topic)-[X]->(:Asset) X does not exist.");
            default:
                throw new IllegalStateException( "Unknown Node Type enum: " + this );
        }
    }

    public String relFromAsset()
    throws IllegalArgumentException, IllegalStateException
    {
        switch ( this )
        {
            case ARTIST:
                return "BY";
            case CLASS:
                return "ASSET_CLASS";
            case CULTURE:
                return "ASSET_CULTURE";
            case NATION:
                return "ASSET_NATION";
            case TAG:
                return "ASSET_TAG";
            case GENRE:
                return "ASSET_GENRE";
            case MEDIUM:
                return "ASSET_MEDIUM";
            case CITY:
                return "ASSET_CITY";
            case FOLDER:
                return "FOLDER_ASSET";
            case OPEN_PIPE_SETTING:
                return "SETTING_OPTION";
            case TOPIC:
                throw new IllegalArgumentException( "The generic 'Topic' label matches all Topics, thus has no specific named relationship.");
            case ASSET:
                throw new IllegalArgumentException( "Assets do not have direct relationships with eachother. e.g.: (:Asset)<-[X]->(:Asset) X does not exist.");
            default:
                throw new IllegalStateException( "Unknown Node Type enum: " + this );
        }
    }

    public static NodeType match(String label)
    throws IllegalArgumentException
    {
        switch (label.toUpperCase()) {
            case "CLASSIFICATION":
                label = "class";
                break;
        }

        // note: Java bug in valueOf? if the entry is not found, valueOf neither completes nor throws an exception
        for ( NodeType n : NodeType.values() ) {
            if ( n.toString().equalsIgnoreCase(label) ) {
                return n;
            }
        }

        throw new IllegalArgumentException(label +" is not a valid NodeType.");
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
            case GENRE:
            case MEDIUM:
            case CITY:
            //case FOLDER:
                return true;
            default:
                return false;
        }
    }

    public static NodeType fromNeoNode(org.neo4j.graphdb.Node node)
    {
        for (Label l : node.getLabels()) {
            String lName = l.name();
            if (lName.equals("Topic")) continue;
            if (lName.equals("Asset")) return ASSET;
            if (lName.equals("Artist")) return ARTIST;
            if (lName.equals("Tag")) return TAG;
            if (lName.equals("Culture")) return CULTURE;
            if (lName.equals("Nation")) return NATION;
            if (lName.equals("Classification")) return CLASS;
            if (lName.equals("Genre")) return GENRE;
            if (lName.equals("Medium")) return MEDIUM;
            if (lName.equals("City")) return CITY;
            if (lName.equals("Folder")) return FOLDER;
            if (lName.equals("OpenPipeSetting")) return OPEN_PIPE_SETTING;
        }

        return null;
    }

    /* @todo refactor this and NodeType calls - can be simplified */
    public String makeRelatedUri(String key)
    {
        switch ( this )
        {
            case ARTIST:
            case CLASS:
            case CULTURE:
            case NATION:
            case TAG:
            case GENRE:
            case MEDIUM:
            case CITY:
            case TOPIC:
            case ASSET:
                return Config.buildUri("/related/" + this.labelName().toLowerCase() + "/" + URLCoder.encode(key));
            case FOLDER:
                return Config.buildUri("/folder/" + URLCoder.encode(key));
            default:
                return null;
        }
    }

    public String makeAssetsUri(String key)
    {
        switch ( this )
        {
            case ARTIST:
            case CLASS:
            case CULTURE:
            case NATION:
            case TAG:
            case GENRE:
            case MEDIUM:
            case CITY:
                return Config.buildUri("/topic-assets/" + this.labelName().toLowerCase() + "/" + URLCoder.encode(key));
            case FOLDER:
                return Config.buildUri("/folder/" + URLCoder.encode(key));
            default:
                return null;
        }
    }
}
