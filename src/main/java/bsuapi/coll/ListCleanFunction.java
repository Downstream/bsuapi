package bsuapi.coll;

import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import java.util.ArrayList;
import java.util.List;

public class ListCleanFunction
{
    @UserFunction
    @Description("bsuapi.coll.cleanOfList([list-to-clean],[list-invalid-entries], bool (return null when empty - default: true)) remove empty, null, and invalid entries from a list. ")
    public List cleanOfList(
        @Name("listToClean") List listToClean,
        @Name("invalid") List invalid,
        @Name(value = "nullify", defaultValue = "true") boolean nullify
    )
    {
        return this.cleanTheList(listToClean, invalid, nullify);
    }

    @UserFunction
    @Description("bsuapi.coll.cleanOf([list-to-clean],'invalid-string', bool (return null when empty - default: true)) remove empty, null, and invalid entries from a list. ")
    public List cleanOf(
        @Name("listToClean") List listToClean,
        @Name("invalid") String invalidString,
        @Name(value = "nullify", defaultValue = "true") boolean nullify
    )
    {
        ArrayList<Object> invalid = new ArrayList<>();
        invalid.add(invalidString);
        if (!invalidString.equals("")) {
            invalid.add("");
        }

        return this.cleanTheList(listToClean, invalid, nullify);
    }

    @UserFunction
    @Description("bsuapi.coll.clean([list-to-clean], bool (return null when empty - default: true)) remove empty, null, and invalid entries from a list. ")
    public List clean(
            @Name("listToClean") List listToClean,
            @Name(value = "nullify", defaultValue = "true") boolean nullify
    )
    {
        ArrayList<Object> invalid = new ArrayList<>();
        invalid.add("");

        return this.cleanTheList(listToClean, invalid, nullify);
    }

    private List cleanTheList(List listToClean, List invalid, boolean nullify)
    {
        ArrayList<Object> cleanList = new ArrayList<>();
        List removeList = this.buildInvalidList(invalid);

        for (Object entry : listToClean) {
            if (entry == null || removeList.contains(entry) || (entry instanceof List && ((List) entry).isEmpty())) {
                continue; // skip invalid (more efficient than inverting the above conditional)
            }

            cleanList.add(entry);
        }

        if (nullify && cleanList.isEmpty()) {
            return null;
        }

        return cleanList;
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
