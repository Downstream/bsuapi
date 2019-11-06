package bsuapi.resource;

import org.json.JSONObject;
import org.neo4j.logging.Log;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Scanner;

public class Util {
    // RESOURCE FILES
    public static String readResourceFile(String filename) {
        return Util.scanner(Util.getResourceFileStream(filename));
    }

    public static JSONObject readResourceJSON(String filename) {
        return new JSONObject(Util.readResourceFile(filename+".json"));
    }

    private static InputStream getResourceFileStream(String filename) {
        return Util.class.getClassLoader().getResourceAsStream(filename);
    }

    private static String scanner(InputStream stream) {
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
}
