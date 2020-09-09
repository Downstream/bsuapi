package bsuapi.obj;

import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ObjectOpenPipeFunction
{
    @UserFunction
    @Description("bsuapi.obj.openPipeCleanObj({object-to-clean},[list-invalid-entries]) remove invalid or empty properties from OpenPipeline data (key -1, default canon entries, empty-string). ")
    public Object openPipeCleanObj(
        @Name("objToClean") Map objToClean,
        @Name("invalid") List invalid
    )
    {
        return this.cleanObj(objToClean, invalid);
    }

    private Object cleanObj(Map objToClean, List invalid)
    {
        if (null == objToClean) {
            return null;
        }

        List removeList = this.buildInvalidObj(invalid);

        for (Object key : objToClean.keySet()) {
            Object entry = objToClean.get(key);
            if (entry == null || removeList.contains(entry) || (entry instanceof List && ((List) entry).isEmpty())) {
                continue;
            }

            return entry;
        }

        return null;
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
