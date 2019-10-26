package bsuapi.resource;

import java.io.InputStream;
import java.util.Scanner;

public class Util {
    public static String readResourceFile(String filename) {
        return Util.scanner(Util.getResourceFileStream(filename));
    }

    private static InputStream getResourceFileStream(String filename) {
        return Util.class.getClassLoader().getResourceAsStream(filename);
    }

    private static String scanner(InputStream stream) {
        return (new Scanner(stream)).useDelimiter("\\Z").next();
    }
}
