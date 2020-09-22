package bsuapi.resource;

import org.apache.commons.lang3.StringUtils;

public class URLCoder
{
    public static String encode(String value)
    {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return "This should not be possible.";
        }
    }

    public static String decode(String value)
    {
        try {
            return java.net.URLDecoder.decode(value, "UTF-8");
        } catch (Exception e) {
            return "This should not be possible.";
        }
    }
}
