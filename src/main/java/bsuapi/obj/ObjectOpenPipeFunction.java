package bsuapi.obj;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectOpenPipeFunction
{
    @UserFunction
    @Description("bsuapi.obj.openPipeCleanObj({object-to-clean},[list-invalid-entries]) remove invalid or empty properties from OpenPipeline data (key -1, default canon entries, empty-string). ")
    public Map openPipeCleanObj(
        @Name("objToClean") Map objToClean,
        @Name("invalid") List invalid
    )
    {
        return this.cleanObj(objToClean, invalid);
    }

    private Map cleanObj(Map objToClean, List invalid)
    {
        if (null == objToClean) {
            return new HashMap<>();
        }

        List removeList = this.buildInvalidObj(invalid);
        HashMap<Object, Object> result = new HashMap<>();

        for (Object key : objToClean.keySet()) {
            Object entry = objToClean.get(key);
            if (this.isValid(key, entry) && !removeList.contains(entry)) {
                result.put(key, entry);
            }
        }

        if (!result.isEmpty()) {
            return result;
        }

        return new HashMap<>();
    }

    private boolean isValid(Object key, Object entry)
    {
        if (entry == null) return false;
        if (entry instanceof List) return false;
        if (entry instanceof String && StringUtils.right((String) key, 2).equals("-1")) return false;

        return true;
    }

    private List buildInvalidObj(Object startList)
    {
        ArrayList<Object> result;
        if (startList instanceof List) {
            result = new ArrayList<Object>((List) startList);
        } else {
            result = new ArrayList<>();
            result.add(startList);
        }

        if (!result.contains("")) {
            result.add("");
        }

        return result;
    }
}
