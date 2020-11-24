package bsuapi.dbal.query;

import bsuapi.dbal.*;
import bsuapi.resource.JsonResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class Timeline extends CypherQuery
implements QueryResultSingleColumn
{
    protected static String query =
        "MATCH (%1$s)<-[r:%2$s]-("+ QueryResultSingleColumn.resultColumn +":%3$s) " +
        "%4$s RETURN "+ QueryResultSingleColumn.resultColumn + " " +
        "ORDER BY "+ QueryResultSingleColumn.resultColumn +".date ASC"
        ;

    protected static String[] clauses = new String[]{
        "EXISTS(" + QueryResultSingleColumn.resultColumn +".date)"
    };

    protected JSONObject results;

    protected Topic topic;
    protected TimeStep step;

    public Timeline(Topic topic)
    throws NullPointerException
    {
        super(Timeline.query);
        this.target = NodeType.ASSET;
        this.topic = topic;
        this.step = TimeStep.stepFromTopic(topic);
    }

    public JSONObject getResults()
    {
        this.results.put("step",this.step);
        return this.results;
    }

    public String getCommand()
    {
        return this.resultQuery = String.format(
            this.initQuery,
            this.topic.toCypherMatch(),
            this.topic.getType().relFromAsset(),
            this.target.labelName(),
            this.where(Timeline.clauses)
        ) + this.getPageLimitCmd();
    }

    public void entryHandler(Object entry)
    {
        try {
            Node asset;
            if (entry instanceof org.neo4j.graphdb.Node) {
                asset = new Node((org.neo4j.graphdb.Node) entry);
            } else if (entry instanceof Map) {
                asset = new VirtualNode((Map) entry);
            } else {
                this.addEntry("error", entry.getClass() + ": " + entry.toString());
                return;
            }

            this.addEntry(this.step.getDateKey(asset), asset.toJsonObject());

        } catch (Throwable e) {
            this.results.put("exeption", JsonResponse.exceptionDetailed(e));
        }
    }

    protected void addEntry(String dateGroup, Object entry)
    {
        if (this.results == null) {
            this.results = new JSONObject();
        }

        JSONArray groupResults = this.results.optJSONArray(dateGroup);

        if (groupResults == null) {
            groupResults = new JSONArray();
        }

        groupResults.put(entry);

        this.results.put(dateGroup, groupResults);
    }
}

enum TimeStep
{
    DAY,
    MONTH,
    YEAR,
    YEAR5,
    YEAR10,
    YEAR20,
    YEAR50,
    YEAR100
    ;

    private static final int DEFAULT_TARGET_STEPS = 100;

    public static TimeStep stepFromTopic(Topic topic)
    throws NullPointerException
    {
        LocalDate startDate = TimeStep.parseLocalDate(topic,"dateStart");
        LocalDate endDate = TimeStep.parseLocalDate(topic,"dateEnd");

        if (startDate == null || endDate == null) {
            throw new NullPointerException("Topic or Folder missing dates, likely has no assets.");
        }

        return TimeStep.stepFromDates(startDate, endDate);
    }

    public static LocalDate parseLocalDate(Topic topic, String property)
    {
        Object date = topic.getRawProperty(property);
        if (date instanceof LocalDate) {
            return (LocalDate) date;
        }

        if (date != null) {
            return LocalDate.parse((String) date);
        }

        return null;
    }

    public static LocalDate parseLocalDate(Node node, String property)
    {
        Object date = node.getRawProperty(property);
        if (date instanceof LocalDate) {
            return (LocalDate) date;
        }

        if (date != null) {
            return LocalDate.parse((String) date);
        }

        return null;
    }

    public static TimeStep stepFromDates(LocalDate start, LocalDate end)
    {
        return TimeStep.stepFromDates(start, end, DEFAULT_TARGET_STEPS);
    }

    public static TimeStep stepFromDates(LocalDate start, LocalDate end, int targetSteps)
    {
        int offset = targetSteps/3;
        long days = ChronoUnit.DAYS.between(start, end);
        long years = ChronoUnit.YEARS.between(start, end);

        if (days        < targetSteps + offset) return TimeStep.DAY;
        if (days/32     < targetSteps + offset) return TimeStep.MONTH;
        if (years       < targetSteps + offset) return TimeStep.YEAR;
        if (years/5     < targetSteps + offset) return TimeStep.YEAR5;
        if (years/10    < targetSteps + offset) return TimeStep.YEAR10;
        if (years/20    < targetSteps + offset) return TimeStep.YEAR20;
        if (years/50    < targetSteps + offset) return TimeStep.YEAR50;

        return TimeStep.YEAR100;
    }

    public String getDateKey(Node node)
    {
        LocalDate date = TimeStep.parseLocalDate(node, "date");

        switch (this)
        {
            case DAY:  return DateTimeFormatter.ofPattern("dd MMM yyyy").format(date);
            case MONTH:  return DateTimeFormatter.ofPattern("MMM yyyy").format(date);
            case YEAR:  return DateTimeFormatter.ofPattern("yyyy").format(date);
            default: return Integer.toString(Math.round(date.getYear()/this.roundingDivisor())*this.roundingDivisor());
        }
    }

    private int roundingDivisor()
    {
        switch (this)
        {
            case YEAR5:  return 5;
            case YEAR10:  return 10;
            case YEAR20:  return 20;
            case YEAR50:  return 50;
            case YEAR100:  return 100;
        }

        return 1;
    }
}
