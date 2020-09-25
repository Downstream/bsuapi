package bsuapi.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config
{
    private String file = "config.properties";
    private static Config singleton;
    private Properties properties;
    private int showErrors = 0;

    private Config()
    {
        this.properties = new Properties();

        try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(this.file)) {
            this.properties.load(resourceAsStream);
            try { this.showErrors = Integer.parseInt(this.properties.getProperty("showErrors")); } catch (Exception ignored) {}
        } catch (IOException e) {
            System.err.println("Unable to load configuration from " + this.file);
        }
    }

    private static Config instance()
    {
        if (null == singleton) {
            singleton = new Config();
        }

        return singleton;
    }

    public static int showErrors()
    {
        return Config.instance().showErrors;
    }

    public static String get(String key)
    {
        try {
            return Config.instance().properties.getProperty(key);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getDefault(String key, String def)
    {
        try {
            return Config.instance().properties.getProperty(key, def);
        } catch (Exception e) {
            return def;
        }
    }

    public static String buildUri(String path)
    {
        String base = Config.get("domain") + Config.get("baseuri");
        if (base.isEmpty()) {
            base = "bsu.downstreamlabs.com/bsuapi";
        }

        return "https://" + base + path;
    }
}
