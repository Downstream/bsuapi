package bsuapi.dbal;

import java.util.HashMap;
import java.util.Map;

public class Topic extends Element
{
    public static final String labelParam = "topicLabel";
    public static final String keyParam = "topicKey";

    public Topic(NodeType nodeType, String nodeKey)
    {
        super(nodeType, nodeKey);
    }

    public static Map<String, String> plainMap(String label, String key)
    {
        Map<String, String> m = new HashMap<>();
        m.put(Topic.labelParam, label);
        m.put(Topic.keyParam, key);

        return m;
    }
}
