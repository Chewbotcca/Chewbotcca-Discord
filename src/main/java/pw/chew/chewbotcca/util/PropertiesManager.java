package pw.chew.chewbotcca.util;

import java.util.Properties;

public class PropertiesManager {
    public static Properties properties;
    public static void loadProperties(Properties config) {
        properties = config;
    }

    /**
     * Bot token from discord
     */
    public static String getToken() {
        return properties.getProperty("token");
    }

    /**
     * Client ID of the bot
     */
    public static String getClientId() {
        return properties.getProperty("client_id");
    }

    /**
     * Top.gg token for DBL/Top.gg bot info command
     */
    public static String getTopggToken() {
        return properties.getProperty("dbl");
    }

    /**
     * Owner ID of the bot, all perms
     */
    public static String getOwnerId() {
        return properties.getProperty("owner_id");
    }

    /**
     * Wordnik token for define command
     */
    public static String getWordnikToken() {
        return properties.getProperty("wordnik");
    }

    /**
     * Google key for YouTube command
     */
    public static String getGoogleKey() {
        return properties.getProperty("google");
    }

    /**
     * Last.fm token for LastFM command
     */
    public static String getLastfmToken() {
        return properties.getProperty("lastfm");
    }

    /**
     * Bot prefix
     */
    public static String getPrefix() {
        return properties.getProperty("prefix");
    }

    /**
     * Github token for github commands
     */
    public static String getGithubToken() {
        return properties.getProperty("github");
    }

    /**
     * Sentry DSN for error tracking
     */
    public static String getSentryDsn() {
        return properties.getProperty("sentry-dsn");
    }

    /**
     * Sentry Enviornment for error tracking
     */
    public static String getSentryEnv() {
        return properties.getProperty("sentry-env");
    }

    /**
     * Chew's API key for Chew's bot profiles/server settings.
     */
    public static String getChewKey() {
        return properties.getProperty("chewkey");
    }
}
