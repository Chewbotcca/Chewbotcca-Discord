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
package pw.chew.chewbotcca.commands.minecraft;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// %^mcserver command
public class MCServerCommand extends Command {

    public MCServerCommand() {
        this.name = "mcserver";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Start typing, this may take a while
        commandEvent.getChannel().sendTyping().queue();
        // Get info from API
        JSONObject data = new JSONObject(RestClient.get("https://api.mcsrvstat.us/2/" + commandEvent.getArgs()));
        ServerInfo info = new ServerInfo(data);
        EmbedBuilder e = new EmbedBuilder();
        e.setTitle("**Server Info For** `" + commandEvent.getArgs() + "`");

        // Set thumbnail to favicon
        // e.setThumbnail(info.getFavicon());

        // If online, green embed, else red
        String online;
        if (info.isOnline()) {
            online = "Online";
            e.setColor(Color.GREEN);
        } else {
            online = "Offline";
            e.setColor(Color.RED);
        }

        // Show other stats too
        e.setDescription(info.getCleanMOTD());
        e.addField("Status", online, true);
        e.addField("Players", info.getOnlinePlayerCount() + "/" + info.getMaxPlayerCount(), true);
        if (info.isGeyser()) {
            e.addField("Bedrock Version", info.getVersion().split(" ")[2], true);
            e.addField("Geyser Version", info.getVersion().split(" ")[1].replaceAll("\\(|\\)", ""), true);
        } else {
            e.addField("Version", info.getVersion(), true);
        }

        // Get last fetch time
        e.setFooter("Last fetched");
        e.setTimestamp(info.getCacheTime());

        commandEvent.reply(e.build());
    }

    /**
     * Parsed object from a mcsrvstat.us response
     */
    private static class ServerInfo {
        private final JSONObject data;

        public ServerInfo(JSONObject data) {
            this.data = data;
        }

        /**
         * @return if the server is online
         */
        public boolean isOnline() {
            return data.getBoolean("online");
        }

        /**
         * @return a "clean" version of the MOTD.
         */
        public String getCleanMOTD() {
            JSONArray motdLines = data.getJSONObject("motd").getJSONArray("clean");
            List<String> motd = new ArrayList<>();
            for (Object line : motdLines) {
                motd.add(String.valueOf(line));
            }
            return String.join("\n", motd);
        }

        /**
         * @return the "Version" string
         */
        public String getVersion() {
            return data.getString("version");
        }

        /**
         * A "Geyser" server is a server running Geyser. A specific version is checked.
         * Only works if Geyser version is build 513 or above
         * @return whether or not this is a Geyser
         */
        public boolean isGeyser() {
            return data.getString("version").startsWith("Geyser");
        }

        /**
         * @return get the online player count
         */
        public int getOnlinePlayerCount() {
            return data.getJSONObject("players").getInt("online");
        }

        /**
         * @return get the max player count
         */
        public int getMaxPlayerCount() {
            return data.getJSONObject("players").getInt("max");
        }

        /**
         * @return the favicon
         */
        @Nullable
        public String getFavicon() {
            return data.has("icon") ? data.getString("icon") : null;
        }

        /**
         * UNIX timestamp of the time the result was cached. Returns 0 when the result was not fetched from cache.
         * @return the last cache time
         */
        public Instant getCacheTime() {
            long cacheTime = data.getJSONObject("debug").getLong("cachetime");
            if (cacheTime == 0) {
                return Instant.now();
            }
            return Instant.ofEpochSecond(cacheTime);
        }
    }
}
