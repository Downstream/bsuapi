package bsuapi.obj;

import bsuapi.dbal.Cypher;
import bsuapi.resource.Config;
import bsuapi.resource.JsonResponse;
import org.json.JSONObject;

public class OpenPipeSettings
{
    private JSONObject data;

    public OpenPipeSettings(){}

    public static JSONObject listAll(Cypher c)
    {
        OpenPipeSettings s = new OpenPipeSettings();
        return s.exec(c);
    }

    private JSONObject exec(Cypher c)
    {
        this.data = new JSONObject();
        for ( SettingGroup group : SettingGroup.active() ) {
            OpenPipeSetting current = new OpenPipeSetting(group);

            try {
                this.data.put(group.key(), current.getData(c));
            } catch (Throwable e) {
                this.data.put(group.key(), JsonResponse.exceptionDetailed(e));
            }
        }

        this.data.put("config", this.getConfig());

        return this.data;
    }

    private JSONObject getConfig()
    {
        JSONObject result = new JSONObject();
        result.put("artifactId",Config.get("artifactId"));
        result.put("domain",Config.get("domain"));
        result.put("baseuri",Config.get("baseuri"));
        result.put("name",Config.get("name"));
        result.put("homeFilter",Config.get("homeFilter"));
        result.put("showErrors",Config.showErrors());
        return result;
    }
}
