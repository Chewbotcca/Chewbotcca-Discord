/*
 * Copyright (C) 2021 Chewbotcca
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package pw.chew.chewbotcca.util;

import java.util.Properties;

// bot.properties manager
public class PropertiesManager {
    public static Properties properties;
    public static void loadProperties(Properties config) {
        properties = config;
    }

    /**
     * @return bot token from discord
     */
    public static String getToken() {
        return properties.getProperty("token");
    }

    /**
     * @return Client ID of the bot
     */
    public static String getClientId() {
        return properties.getProperty("client_id");
    }

    /**
     * @return discord.bots.gg token for info command
     */
    public static String getDbotsToken() {
        return properties.getProperty("dbots");
    }

    /**
     * @return Top.gg token for DBL/Top.gg bot info command
     */
    public static String getTopggToken() {
        return properties.getProperty("dbl");
    }

    /**
     * @return Owner ID of the bot, all perms
     */
    public static String getOwnerId() {
        return properties.getProperty("owner_id");
    }

    /**
     * @return Wordnik token for define command
     */
    public static String getWordnikToken() {
        return properties.getProperty("wordnik");
    }

    /**
     * @return Google key for YouTube command
     */
    public static String getGoogleKey() {
        return properties.getProperty("google");
    }

    /**
     * @return Last.fm token for LastFM command
     */
    public static String getLastfmToken() {
        return properties.getProperty("lastfm");
    }

    /**
     * @return Bot prefix
     */
    public static String getPrefix() {
        return properties.getProperty("prefix");
    }

    /**
     * @return Github token for github commands
     */
    public static String getGithubToken() {
        return properties.getProperty("github");
    }

    /**
     * @return Sentry DSN for error tracking
     */
    public static String getSentryDsn() {
        return properties.getProperty("sentry-dsn");
    }

    /**
     * @return Sentry Environment for error tracking
     */
    public static String getSentryEnv() {
        return properties.getProperty("sentry-env");
    }

    /**
     * @return a DiscordExtremeList api token
     */
    public static String getDELToken() {
        return properties.getProperty("del");
    }

    /**
     * @return the key for paste.gg
     */
    public static String getPasteGgKey() {
        return properties.getProperty("pastegg");
    }

    /**
     * @return the Memerator API key
     */
    public static String getMemeratorKey() {
        return properties.getProperty("memerator");
    }

    /**
     * @return the Database Host
     */
    public static String getDatabaseHost() {
        return properties.getProperty("database_host");
    }

    /**
     * @return the Database username
     */
    public static String getDatabaseUsername() {
        return properties.getProperty("database_user");
    }

    /**
     * @return the Database password
     */
    public static String getDatabasePassword() {
        return properties.getProperty("database_pass");
    }
}
