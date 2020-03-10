package bsuapi.coll;

import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import java.util.ArrayList;
import java.util.List;

public class ListSingleFunction
{
    @UserFunction
    @Description("bsuapi.coll.singleCleanList([list-to-clean],[list-invalid-entries]) get first or null from list not in list of invalid entries. ")
    public Object singleCleanList(
        @Name("listToClean") List listToClean,
        @Name("invalid") List invalid
    )
    {
        return this.singleFromList(listToClean, invalid);
    }

    @UserFunction
    @Description("bsuapi.coll.singleCleanOf([list-to-clean],'invalid-string') get first or null from list that is not the invalid string arg. ")
    public Object singleCleanOf(
        @Name("listToClean") List listToClean,
        @Name("invalid") String invalidString
    )
    {
        ArrayList<Object> invalid = new ArrayList<>();
        invalid.add(invalidString);
        if (!invalidString.equals("")) {
            invalid.add("");
        }

        return this.singleFromList(listToClean, invalid);
    }

    @UserFunction
    @Description("bsuapi.coll.singleClean([list-to-clean]) get first or null valid value from list. ")
    public Object singleClean(
            @Name("listToClean") List listToClean
    )
    {
        ArrayList<Object> invalid = new ArrayList<>();
        invalid.add("");

        return this.singleFromList(listToClean, invalid);
    }

    private Object singleFromList(List listToClean, List invalid)
    {
        List removeList = this.buildInvalidList(invalid);

        for (Object entry : listToClean) {
            if (entry == null || removeList.contains(entry) || (entry instanceof List && ((List) entry).isEmpty())) {
                continue;
            }

            return entry;
        }

        return null;
    }

    private List buildInvalidList(Object startList)
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
