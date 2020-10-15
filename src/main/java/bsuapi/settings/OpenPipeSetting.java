package bsuapi.settings;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.query.CypherQuery;
import bsuapi.resource.Config;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;

public class OpenPipeSetting
{
    private SettingGroup group;

    public OpenPipeSetting(SettingGroup group)
    {
        this.group = group;
    }

    public JSONObject getData(Cypher c)
    throws Throwable
    {
        CypherQuery query = this.group.query();
        return this.parseData(query.exec(c));
    }

    private JSONObject parseData(JSONArray source)
    {
        switch (this.group) {
            case COLOR:
                return this.colorData(source);
            case TIMELINE:
            case GLOBE:
            case EXPLORE:
            case CONNECTION:
            default:
                return this.defaultData(source);
        }
    }

    private JSONObject defaultData(JSONArray source)
    {
        JSONObject result = new JSONObject();
        result.put("options", source);
        return result;
    }

    private JSONObject colorData(JSONArray source)
    {
        JSONObject data = source.getJSONObject(0);
        JSONObject result = new JSONObject();
        result.put("message", "Welcome to the Keith and Catherine Stein World Museum");
        if (data.has("message") && (data.get("message") instanceof JSONArray)) {
            result.put("message", ((JSONArray) data.get("message")).get(0));
        } else {
            result.put("message", data.get("message"));
        }

        if (!(data.get("colors") instanceof String[])) {return result;}

        JSONArray newColors = new JSONArray();
        try {
            for (String entry : (String[]) data.get("colors")) {
                newColors.put(this.decodeColorSetting(entry));
            }
            result.put("colors", newColors);
        } catch (Throwable e) {
            if (Config.showErrors() > 0) {
                result.put("error", "could not parse configured color: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                result.put("error-cause", JSONObject.valueToString(data.get("colors")));
            } else {
                result.put("error", "colors invalid format: " + JSONObject.valueToString(data.get("colors")));
            }
        }

        return result;
    }

    private JSONObject decodeColorSetting(String colorSetting)
    {
        JSONObject result = new JSONObject();

        result.put("raw", colorSetting);
        result.put("isColor", false);

        if (colorSetting.contains(",") && colorSetting.contains(";") && colorSetting.contains("#")) {
            // parse color pattern:    "#[hex-color-1],#[hex-color-2];[color-set-name]"
            String[] chunks = colorSetting.split("[,;]");
            result.put("color1", this.decodeSingleColor(chunks[0]));
            result.put("color2", this.decodeSingleColor(chunks[1]));
            result.put("title", chunks[2]);
            result.put("isColor", true);
        }

        return result;
    }

    private JSONObject decodeSingleColor(String colorStr)
    {
        JSONObject result = new JSONObject();
        result.put("hex",colorStr);

        Color c = Color.decode(colorStr);
        JSONArray rgba = new JSONArray();
        rgba.put(((float)c.getRed())/255f);
        rgba.put(((float)c.getGreen())/255f);
        rgba.put(((float)c.getBlue())/255f);
        rgba.put(((float)c.getAlpha())/255f);
        result.put("rgba",rgba);

        return result;
    }
}
