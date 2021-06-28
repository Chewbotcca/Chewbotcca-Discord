/*
 * Copyright (C) 2020 Chewbotcca
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
package pw.chew.chewbotcca.objects;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.JDA;
import org.kohsuke.github.GitHub;
import pro.chew.api.ChewAPI;

/**
 * The bot's "Memory", that is, objects to be stored here for later use
 */
public class Memory {
    private static EventWaiter waiter;
    private static JDA jda;
    private static ChewAPI chewAPI;
    private static GitHub github;
    private static CommandClient client;

    public static void remember(EventWaiter waiter, JDA jda, ChewAPI chewAPI, GitHub github, CommandClient client) {
        Memory.waiter = waiter;
        Memory.jda = jda;
        Memory.chewAPI = chewAPI;
        Memory.github = github;
        Memory.client = client;
    }

    /**
     * @return the JDA instance used to run the bot
     */
    public static JDA getJda() {
        return jda;
    }

    /**
     * @return the ChewAPI used for Chew API stuff
     */
    public static ChewAPI getChewAPI() {
        return chewAPI;
    }

    /**
     * @return the GitHub API used for GitHub API stuff
     */
    public static GitHub getGithub() {
        return github;
    }

    /**
     * @return the event waiter used by JDA Utilities
     */
    public static EventWaiter getWaiter() {
        return waiter;
    }

    /**
     * @return the command client used by JDA Utilities
     */
    public static CommandClient getClient() {
        return client;
    }
}
