package bsuapi.obj;

import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import java.util.HashMap;
import java.util.Map;

public class ObjectOpenPipeDate
{
    // BC 500 FEB 13 12:31:14
    //     TO
    // { year:-499, month:2, day:13, hour:12, minute:31, second:14 }

    private static final HashMap<String, Integer> FALLBACK;
    static {
        FALLBACK = new HashMap<>();
        FALLBACK.put("year", 0);
    }

    @UserFunction
    @Description("bsuapi.obj.openPipeDateMap({openpipeDate}) covert openpipe date format into alpha-sortable format. ")
    public Map openPipeDateMap(
        @Name("openpipeDate") String openpipeDate
    )
    {
        if (openpipeDate == null) return ObjectOpenPipeDate.FALLBACK;
        return this.openPipeToMap(openpipeDate);
    }

    private Map openPipeToMap(String openpipeDate)
    {
        String[] dateChunks = openpipeDate.split("\\s+");
        if (dateChunks.length < 5) { return ObjectOpenPipeDate.FALLBACK; }

        HashMap<String, Integer> result = new HashMap<>();

        result.put("year", this.convertYear(dateChunks[0], dateChunks[1]));
        result.put("month", this.convertMonth(dateChunks[2]));
        result.put("day", this.toInt(dateChunks[3]));

        if (dateChunks[4].equals("00:00:00")) return result;

        String[] timeChunks = dateChunks[4].split(":");
        result.put("hour", this.toInt(timeChunks[0]));
        result.put("minute", this.toInt(timeChunks[1]));
        result.put("second", this.toInt(timeChunks[2]));

        return result;
    }

    private int convertYear(String openpipeEpoch, String openpipeYear)
    {
        int result = this.toInt(openpipeYear);
        switch (openpipeEpoch) {
            case "BC":
                return 1 + (-1 * result);
            case "CE":
            default:
                return result;
        }
    }

    private int convertMonth(String openpipeMonth)
    {
        switch (openpipeMonth.toUpperCase()) {
            case "JAN": return 1;
            case "FEB": return 2;
            case "MAR": return 3;
            case "APR": return 4;
            case "MAY": return 5;
            case "JUN": return 6;
            case "JUL": return 7;
            case "AUG": return 8;
            case "SEP": return 9;
            case "OCT": return 10;
            case "NOV": return 11;
            case "DEC": return 12;
            default:    return 1;
        }
    }

    private int toInt(String val)
    {
        // using exceptions like this isn't great, but it's the only way to do so 100% reliably. Thank you Java.
        // fortunately, we should NEVER have non-ints here, so it /should?/ be ok?
        try {
            return Integer.parseInt(val);
        } catch(NumberFormatException e) {
            return 0;
        }
    }
}
