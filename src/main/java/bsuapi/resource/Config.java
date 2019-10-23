package bsuapi.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private String file = "config.properties";
    private static Config singleton;
    private Properties properties;

    private Config() {
        this.properties = new Properties();

        try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(this.file)) {
            this.properties.load(resourceAsStream);
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

    public static String get(String key) {
        try {
            return Config.instance().properties.get(key).toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getDefault(String key, String def) {
        try {
            return Config.instance().properties.getOrDefault(key, def).toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static String buildUri(String path) {
        String base = Config.get("domain") + Config.get("baseuri");
        if (base.isEmpty()) {
            base = "bsu.downstreamlabs.com/bsuapi";
        }

        return "https://" + base + path;
    }
}
