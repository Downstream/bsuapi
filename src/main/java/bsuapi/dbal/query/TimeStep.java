package bsuapi.dbal.query;

import bsuapi.dbal.Node;
import bsuapi.dbal.Topic;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public enum TimeStep
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
            case DAY:  return DateTimeFormatter.ofPattern("d MMM yyyy").format(date);
            case MONTH:  return DateTimeFormatter.ofPattern("MMM yyyy").format(date);
            case YEAR:  return DateTimeFormatter.ofPattern("yyyy").format(date);
            default:
                int year = date.getYear();
                int div = this.roundingDivisor();
                int yearFrac = Math.round(year/div);
                int group = yearFrac*div;
                if (year <0 && group != year) {group -=div;} // group -250 {-0201 to -0250 } ; group -200 { -151 to -200 }
                return Integer.toString(group);
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
