package bsuapi.behavior;

import bsuapi.dbal.*;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.dbal.query.FolderAssets;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class Folder extends Behavior
{
    private JSONArray assets;
    public Topic topic;
    public Node node;
    public String query;

    public Folder(Map<String, String> config)
    throws BehaviorException
    {
        super(config);
        String labelName = this.getConfigParam(Topic.labelParam);
        String keyName = this.getConfigParam(Topic.keyParam);

        if (null == labelName || null == keyName) {
            throw new BehaviorException("Missing required parameters for folder: "+ Topic.labelParam +" and "+ Topic.keyParam);
        }

        this.topic = new Topic(NodeType.match(labelName), keyName);
    }

    @Override
    public String getBehaviorKey() { return "folder"; }

    @Override
    public JSONArray getBehaviorData() { return this.assets; }

    @Override
    public String buildMessage()
    {
        if (this.topic == null) {
            return "No Match Found";
        } else if (this.topic.hasMatch()) {
            return "Found :"+ this.topic.name() +" {"+ this.topic.getNodeKeyField() +":'"+ this.topic.getNodeKey() +"'}";
        } else {
            return "No Match Found For :"+ this.topic.name();
        }
    }

    private void resolveTopic(Cypher cypher)
            throws CypherException
    {
        if (!this.topic.hasMatch()) { cypher.resolveNode(this.topic); }
        this.node = topic.getNode();
    }

    @Override
    public void resolveBehavior(Cypher cypher)
            throws CypherException
    {
        this.resolveTopic(cypher);

        CypherQuery query = new FolderAssets(this.topic);
        this.setQueryConfig(query);
        this.assets = query.exec(cypher);
        this.query = query.getCommand();
        super.resolveBehavior(cypher);
    }

    @Override
    public JSONObject toJson()
    {
        JSONObject data = super.toJson();
        data.put("node", this.topic.toJson());
        data.put("query", this.query);
        return data;
    }

    public static BehaviorDescribe describe()
    {
        BehaviorDescribe desc = BehaviorDescribe.resource("/folder/{GUID}",
            "List all assets, with layout/positional info if available, along with any Topics included in that Folder. " +
            "omit the GUID to get a list of folders. "
        );

        desc.arg("GUID", "URL-encoded string, defined from source. Must start with a letter, a-zA-Z.");

        return desc;
    }
}
