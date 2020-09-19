package bsuapi.obj;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Str;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectOpenPipeDate
{
    @UserFunction
    @Description("bsuapi.obj.openPipeDate({openpipeDate}) covert openpipe date format into alpha-sortable format. ")
    public String openPipeDate(
        @Name("openpipeDate") String openpipeDate
    )
    {
        return this.openPipeToSortable(openpipeDate);
    }

    private String openPipeToSortable(String openpipeDate)
    {
        // BC 500 JAN 01 00:00:00
        // CE 1927 JAN 01 00:00:00
        String[] dateChunks = openpipeDate.split("\\s+");
        if (dateChunks.length < 5 ) { return "0-01-01 00:00:00"; }


        return String.format("%1$s-%2$s-%3$s %4$s",
            this.convertYear(dateChunks[0], dateChunks[1]),
            this.convertMonth(dateChunks[2]),
            this.convertDay(dateChunks[3]),
            dateChunks[4]
        );
    }

    private String convertYear(String openpipeEpoch, String openpipeYear)
    {
        String result;
        switch (openpipeEpoch) {
            case "BC":
                result = "-";
                break;
            case "CE":
            default:
                result = "";
        }

        return result + openpipeYear;
    }

    private String convertMonth(String openpipeMonth)
    {
        switch (openpipeMonth.toUpperCase()) {
            case "JAN": return "01";
            case "FEB": return "02";
            case "MAR": return "03";
            case "APR": return "04";
            case "MAY": return "05";
            case "JUN": return "06";
            case "JUL": return "07";
            case "AUG": return "08";
            case "SEP": return "09";
            case "OCT": return "10";
            case "NOV": return "11";
            case "DEC": return "12";
            default:    return "00";
        }
    }

    private String convertDay(String openpipeDay)
    {
        try {
            return String.format("%02d",Integer.parseInt(openpipeDay));
        } catch(NumberFormatException e) {
            return "00";
        }
    }
}
