package bsuapi.dbal;

import org.json.JSONObject;
import org.neo4j.graphdb.Node;

import java.util.Map;
import java.lang.String;
import java.lang.Object;

class NodeUtil
{
    static JSONObject toJsonObject(Node node)
    {
        JSONObject result = new JSONObject();
        for (Map.Entry<String, Object> entry : node.getAllProperties().entrySet())
        {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
