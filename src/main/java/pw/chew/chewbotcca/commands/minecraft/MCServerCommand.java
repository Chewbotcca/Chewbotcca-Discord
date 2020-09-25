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
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

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
        JSONObject data = new JSONObject(RestClient.get("https://eu.mc-api.net/v3/server/ping/" + commandEvent.getArgs()));
        EmbedBuilder e = new EmbedBuilder();
        e.setTitle("**Server Info For** `" + commandEvent.getArgs() + "`");
        // If there's an error
        if(data.has("error")) {
            e.setColor(Color.decode("#ff0000"));
            e.addField("Error", data.getString("error"), true);
            commandEvent.reply(e.build());
            return;
        }
        // Set thumbnail to favicon
        e.setThumbnail(data.getString("favicon"));

        // If online, green embed, else red
        String online;
        if (data.getBoolean("online")) {
            online = "Online";
            e.setColor(Color.decode("#00ff00"));
        } else {
            online = "Offline";
            e.setColor(Color.decode("#FF0000"));
        }

        // Show other stats too
        e.addField("Status", online, true);
        e.addField("Players", data.getJSONObject("players").getInt("online") + "/" + data.getJSONObject("players").getInt("max"), true);
        e.addField("Version", data.getJSONObject("version").getString("name"), true);

        // Parse date because Java weird
        String fetched = data.getString("fetch");
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX");
        OffsetDateTime odtInstanceAtOffset = OffsetDateTime.parse(fetched, DATE_TIME_FORMATTER);
        e.setFooter("Last fetched");
        e.setTimestamp(odtInstanceAtOffset);

        commandEvent.reply(e.build());
    }
}
