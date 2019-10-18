package bsuapi.behavior;
import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.Topic;

import java.util.Map;

public enum BehaviorType {
    RELATED,
    ASSETS;

    private Behavior prepare(Topic t, Cypher c)
    throws CypherException
    {
        if (!t.hasMatch()) { c.resolveTopic(t); }

        Behavior b;
        switch ( this ) {
            case RELATED:
                b = new Related(t);
                b.appendBehavior(BehaviorType.ASSETS.prepare(t, c));
                return b;
            case ASSETS:
                b = new Assets(t);
                return b;
        }

        return null;
    }

    public Behavior compose(Topic t, Cypher c)
    throws CypherException
    {

        Behavior b = this.prepare(t, c);
        if (b != null) {
            return this.resolve(b, c);
        }

        return null;
    }

    public Behavior compose(Topic t, Cypher c, Map<String, String> config)
    throws CypherException
    {
        Behavior b = this.prepare(t, c);
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
