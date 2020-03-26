package bsuapi.resource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.logging.Log;

import java.io.InputStream;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Iterator;
import java.util.Scanner;

public class Util
{
    // RESOURCE FILES
    public static String readResourceFile(String filename)
    throws Exception
    {
        try {
            return Util.scanner(Util.getResourceFileStream(filename));
        } catch (Exception e) {
            throw new Exception("Could not read resource file: "+ filename, e);
        }
    }

    public static JSONObject readResourceJSON(String filename)
    throws Exception
    {
        return new JSONObject(Util.readResourceFile(filename+".json"));
    }

    public static String resourceFile(String filename)
    {
        try {
            return Util.readResourceFile(filename);
        } catch (Exception e) {
            return "Error reading file: "+e.getMessage();
        }
    }

    public static JSONObject resourceJSON(String filename)
    {
        try {
            return Util.readResourceJSON(filename);
        } catch (Exception e) {
            JSONObject result = new JSONObject();
            result.put("error", true);
            result.put("file", filename+".json");
            result.put("summary", "File missing or malformed.");
            result.put("message", e.getMessage());
            result.put("exception", e);
            return result;
        }
    }

    private static InputStream getResourceFileStream(String filename)
    {
        return Util.class.getClassLoader().getResourceAsStream(filename);
    }

    private static String scanner(InputStream stream)
    throws Exception
    {
        if (null == stream) {
            throw new Exception("Stream empty. Most likely cause: file not found.");
        }
        return (new Scanner(stream)).useDelimiter("\\Z").next();
    }


    // EXCEPTIONS
    public static void logException(Log log, Exception e)
    {
        log.error(Config.get("artifactId") + " Exception: ", e);
    }

    public static void logException(Log log, Exception e, String message)
    {
        log.error(message, e);
    }

    public static void logFormattedException(Log log, Exception e)
    {
        Throwable cause = e;
        StringWriter w = new StringWriter();
        do {
            for(StackTraceElement trace : cause.getStackTrace()) {
                String entry = trace.getFileName() +"["+ trace.getLineNumber() +"] "+ trace.getClassName() +"."+ trace.getMethodName() +"()";
                w.write(Util.formatLogLine(entry, 2));
            }
            w.write(Util.formatLogLine(cause.getClass().getSimpleName(), 0));
        } while(null != (cause = e.getCause()));

        log.error(w.toString());
    }

    private static String formatLogLine(String message, int margin)
    {
        String indent = (margin>0)
            ? new String(new char[margin]).replace("\0", " ")
            : ""
            ;
        return "\n" + indent + message;
    }


    // convenience methods
    public static Object jsonArrayFirst(JSONArray j)
    {
        Iterator i = j.iterator();
        if (!i.hasNext()) { return null; }
        return i.next();
    }

    public static String durationDisplayFormat(Duration d)
    {
        long hours = d.toHours();
        long minutes = d.minusHours(hours).toMinutes();
        long millis = d.minusHours(hours).minusMinutes(minutes).toMillis();
        long seconds = millis / 1000;
        millis = millis % 1000;
        return String.format("%d:%02d:%02d:%03d",hours,minutes,seconds,millis);
    }
}
