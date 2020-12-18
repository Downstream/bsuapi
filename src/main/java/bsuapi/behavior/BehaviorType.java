package bsuapi.behavior;
import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;

import java.util.Map;

public enum BehaviorType
{
    RELATED,
    ASSETS,
    FOLDER,
    SEARCH,
    SEARCH_ASSETS,
    SEARCH_TOPICS,
    SEARCH_FOLDERS;

    private Behavior prepare(Map<String, String> config)
    throws BehaviorException
    {
        Behavior b;
        switch ( this )
        {
            case RELATED:
                b = new Related(config);
                b.appendBehavior(BehaviorType.ASSETS.prepare(config));
                return b;

            case ASSETS:
                b = new Assets(config);
                return b;

            case FOLDER:
                b = new Folder(config);
                b.appendBehavior(new Related(config));
                return b;

            case SEARCH:
                b = new Search(config);
                b.appendBehavior(BehaviorType.SEARCH_ASSETS.prepare(config));
                b.appendBehavior(BehaviorType.SEARCH_TOPICS.prepare(config));
                b.appendBehavior(BehaviorType.SEARCH_FOLDERS.prepare(config));
                return b;

            case SEARCH_ASSETS:
                b = new AssetIndex(config);
                return b;

            case SEARCH_TOPICS:
                b = new TopicIndex(config);
                return b;
            case SEARCH_FOLDERS:
                b = new FolderIndex(config);
                return b;
        }

        return null;
    }

    public Behavior compose(Cypher c, Map<String, String> config)
    throws CypherException, BehaviorException
    {
        Behavior b = this.prepare(config);

        if (b != null) {
            return this.resolve(b, c);
        }

        return null;
    }

    public Behavior resolve(Behavior b, Cypher c)
    throws CypherException
    {
        b.resolveBehavior(c);
        return b;
    }
}
