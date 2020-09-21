package bsuapi.resource;

import org.apache.commons.lang3.StringUtils;

public class URLCoder
{
    public static String encode(String value)
    {
        try {
            // there's something odd going on with requests in production. Nginx decoding before reverse proxy?
            if (StringUtils.substring(value, 0, 4).equals("http")) {
                value = java.net.URLEncoder.encode(value, "UTF-8");
            }
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return "This should not be possible.";
        }
    }

    public static String decode(String value)
    {
        try {
            if (value.indexOf("%") > 0 || StringUtils.substring(value, 0, 4).equals("http")) {
                value = java.net.URLDecoder.decode(value, "UTF-8");
            }
            return java.net.URLDecoder.decode(value, "UTF-8");
        } catch (Exception e) {
            return "This should not be possible.";
        }
    }
}
