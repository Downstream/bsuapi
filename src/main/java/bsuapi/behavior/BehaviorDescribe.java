package bsuapi.behavior;

import org.json.JSONObject;

public class BehaviorDescribe extends JSONObject {
    private JSONObject arguments;

    public static BehaviorDescribe resource(String uri, String description)
    {
        BehaviorDescribe desc = new BehaviorDescribe();
        desc.put("uri", uri);
        desc.put("description", description);
        desc.put("representation", "TBD");
        return desc;
    }

    public void example(Object representation)
    {
        this.put("representation", representation);
    }

    public void arg(String name, String description)
    {
        if (this.arguments == null) {
            this.arguments = new JSONObject();
        }

        this.arguments.put(name, description);
        this.put("args",this.arguments);
    }
}
