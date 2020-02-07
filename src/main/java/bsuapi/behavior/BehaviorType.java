package bsuapi.behavior;
import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.Topic;

import java.util.Map;

public enum BehaviorType {
    RELATED,
    ASSETS;

    private Behavior prepare(Map<String, String> config)
    {
        Behavior b;
        switch ( this ) {
            case RELATED:
                b = new Related(config);
                b.appendBehavior(BehaviorType.ASSETS.prepare(config));
                return b;
            case ASSETS:
                b = new Assets(config);
                return b;
        }

        return null;
    }

    public Behavior compose(Cypher c, Map<String, String> config)
            throws CypherException
    {
        Behavior b = this.prepare(config);

        if (b != null) {
            b.setConfig(config);
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
