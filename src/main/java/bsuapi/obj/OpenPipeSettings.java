package bsuapi.obj;

import bsuapi.dbal.NodeType;
import bsuapi.resource.Config;
import bsuapi.resource.JsonResponse;
import bsuapi.resource.URLCoder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Color;
import java.util.List;

public class OpenPipeSettings
{
    protected static String KEYFIELD = "name";
    JSONObject data;
    private String name = "undefined";
    private boolean valid = false;

    public OpenPipeSettings(JSONObject data)
    {
        this.data = this.parseData(data);
    }

    public boolean isValid() { return this.valid; }

    public String name()
    {
        return this.name;
    }

    public JSONObject data()
    {
        return this.data;
    }

    private JSONObject parseData(JSONObject source)
    {
        JSONObject result = new JSONObject();
        if (!(source.has(KEYFIELD) && (source.get(KEYFIELD) instanceof String))) {
            this.valid = false;
            result.put("error", "Color settings missing keyfield: "+ KEYFIELD);
            return result;
        }

        try {
            this.name = (String) source.get(KEYFIELD);
            source.remove(KEYFIELD);
            source.remove("keyField");
            source.remove("keyEncoded");
            source.remove("type");

            this.valid = true;
            switch (this.name) {
                case "color":
                    return this.colorData(source);
                case "timeline":
                    return this.timelineData(source);
                case "globe":
                case "explore":
                case "connections":
                default:
                    return source;
            }
        } catch (Exception e) {
            result.put("exception", this.exceptionHandler(e));
            this.valid = (Config.showErrors() > 0);
            return result;
        }
    }

    private JSONObject colorData(JSONObject source)
    {
        JSONObject result = new JSONObject();
        result.put("message", "Welcome to the Keith and Catherine Stein World Museum");
        if (source.has("message") && (source.get("message") instanceof JSONArray)) {
            result.put("message", ((JSONArray) source.get("message")).get(0));
        } else {
            result.put("message", source.get("message"));
        }

        if (!(source.get("colors") instanceof String[])) {return result;}

        JSONArray newColors = new JSONArray();
        try {
            for (String entry : (String[]) source.get("colors")) {
                newColors.put(this.decodeColorSetting(entry));
            }
            result.put("colors", newColors);
        } catch (Exception e) {
            if (Config.showErrors() > 0) {
                result.put("error", "could not parse configured color: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                result.put("error-cause", JSONObject.valueToString(source.get("colors")));
            } else {
                result.put("error", "colors invalid format: " + JSONObject.valueToString(source.get("colors")));
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

    private JSONObject timelineData(JSONObject source)
    {
        JSONObject result = new JSONObject();
        JSONArray newTimeline = new JSONArray();
        this.valid = false;

        if (!(source.get("preset") instanceof String[])) {return result;}

        for (String entry : (String[]) source.get("preset")) {

            // parse timeline pattern:    "[guid]" OR "[guid] by [TopicType]"
            JSONObject timelineOption = new JSONObject();

            String guid = entry;
            String uri;
            String[] chunks = entry.split("\\s+");
            if (chunks.length == 3 && "by".equals(chunks[1])) {
                guid = chunks [0];
                try {
                    NodeType topicType = NodeType.match(chunks[2]);
                    if (topicType.isTopic()) {
                        uri = Config.buildUri("/" + topicType.labelName().toLowerCase() + "/" + URLCoder.encode(guid));
                        timelineOption.put("linkRelated", uri);
                        timelineOption.put("topicType", topicType.labelName());
                    }
                } catch (IllegalArgumentException ignored) {}
            }

            timelineOption.put("guid", guid);

            newTimeline.put(timelineOption);
        }

        if (newTimeline.length() > 0) {
            this.valid = true;
            result.put("preset", newTimeline);
            return result;
        }

        return result;
    }

    private JSONObject exceptionHandler(Exception e) {
        JSONObject exObj = new JSONObject();
        exObj.put("message", e.getMessage());
        exObj.put("cause", e.getCause());
        if (Config.showErrors() > 1) {
            exObj.put("stack", JsonResponse.exceptionStack(e));
        }
        return exObj;
    }
}
