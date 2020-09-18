package bsuapi.dbal;

import bsuapi.resource.Util;
import java.util.Map;

public class VirtualNode extends Node
{
    public VirtualNode(Map vnode)
    {
        try {
            this.type = NodeType.match((String) vnode.get("type"));
        } catch (IllegalArgumentException ignored) {
            /* @todo !IMPORTANT we need to throw this, or similar, but it'll take a refactor */
            //throw new CypherException("Virtual Node does not specify a valid NodeType. found: "+ vnode.get("type"));
        }

        this.properties = Util.mapToStringObject(vnode);
        this.keyVal = this.getProperty(this.keyName);

    }

    public org.neo4j.graphdb.Node getNeoNode()
    throws CypherException
    {
        throw new CypherException("Cannot retrieve the NeoNode of a VirtualNode");
    }

    public NodeType getType()
    {
        return this.type;
    }
}
