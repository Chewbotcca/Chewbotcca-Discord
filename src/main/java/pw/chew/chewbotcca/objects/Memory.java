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

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.mcprohosting.MCProHostingAPI;
import net.dv8tion.jda.api.JDA;
import org.discordbots.api.client.DiscordBotListAPI;
import org.kohsuke.github.GitHub;
import pro.chew.api.ChewAPI;

/**
 * The bot's "Memory", that is, objects to be stored here for later use
 */
public class Memory {
    private static EventWaiter waiter;
    private static JDA jda;
    private static ChewAPI chewAPI;
    private static MCProHostingAPI mcproAPI;
    private static GitHub github;
    private static DiscordBotListAPI topgg;

    public Memory(EventWaiter waiter, JDA jda, ChewAPI chewAPI, MCProHostingAPI mcproAPI, GitHub github, DiscordBotListAPI topgg) {
        Memory.waiter = waiter;
        Memory.jda = jda;
        Memory.chewAPI = chewAPI;
        Memory.mcproAPI = mcproAPI;
        Memory.github = github;
        Memory.topgg = topgg;
    }

    public static JDA getJda() {
        return jda;
    }

    public static void setJda(JDA jda) {
        Memory.jda = jda;
    }

    public static ChewAPI getChewAPI() {
        return chewAPI;
    }

    public void setChewAPI(ChewAPI chewAPI) {
        Memory.chewAPI = chewAPI;
    }

    public static MCProHostingAPI getMcproAPI() {
        return mcproAPI;
    }

    public void setMcproAPI(MCProHostingAPI mcproAPI) {
        Memory.mcproAPI = mcproAPI;
    }

    public static GitHub getGithub() {
        return github;
    }

    public void setGithub(GitHub github) {
        Memory.github = github;
    }

    public static EventWaiter getWaiter() {
        return waiter;
    }

    public void setWaiter(EventWaiter waiter) {
        Memory.waiter = waiter;
    }

    public static DiscordBotListAPI getTopgg() {
        return topgg;
    }

    public static void setTopgg(DiscordBotListAPI topgg) {
        Memory.topgg = topgg;
    }
}
