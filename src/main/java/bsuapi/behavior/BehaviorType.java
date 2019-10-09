package bsuapi.behavior;
import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.Topic;

public enum BehaviorType {
    RELATED,
    ASSETS;

    public Behavior compose(Topic t, Cypher c)
    throws CypherException
    {
        Behavior b;
        switch ( this ) {
            case RELATED:
                b = new Related(t);
                b.resolveBehavior(c);
                b.appendBehavior(BehaviorType.ASSETS.compose(t, c));
                return b;
            case ASSETS:
                b = new Assets(t);
                b.resolveBehavior(c);
                return b;
        }

        return null;
    }
}
