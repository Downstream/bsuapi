package bsuapi.obj;

import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ObjectSingleFunction
{
    @UserFunction
    @Description("bsuapi.obj.singleCleanObj({object-to-clean},[list-invalid-entries]) get first or null from object property values not in list of invalid entries. ")
    public Object singleCleanList(
        @Name("objToClean") Map objToClean,
        @Name("invalid") List invalid
    )
    {
        return this.singleFromObj(objToClean, invalid);
    }

    @UserFunction
    @Description("bsuapi.obj.singleCleanOf({object-to-clean},'invalid-string') get first or null from object property values that are not the invalid string arg. ")
    public Object singleCleanOf(
        @Name("objToClean") Map objToClean,
        @Name("invalid") String invalidString
    )
    {
        ArrayList<Object> invalid = new ArrayList<>();
        invalid.add(invalidString);
        if (!invalidString.equals("")) {
            invalid.add("");
        }

        return this.singleFromObj(objToClean, invalid);
    }

    @UserFunction
    @Description("bsuapi.obj.singleClean({object-to-clean}) get first or null valid value from object properties. ")
    public Object singleClean(
        @Name("objToClean") Map objToClean
    )
    {
        ArrayList<Object> invalid = new ArrayList<>();
        invalid.add("");

        return this.singleFromObj(objToClean, invalid);
    }

    private Object singleFromObj(Map objToClean, List invalid)
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
