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
import org.json.JSONArray;
import org.json.JSONException;
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
            e.setColor(Color.GREEN);
        } else {
            online = "Offline";
            e.setColor(Color.RED);
        }

        // Show other stats too
        e.setDescription(generateDescription(data).replaceAll("§([a-f]|[k-o]|r|[0-9])", ""));
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

    public String generateDescription(JSONObject data) {
        if (!data.has("description") || data.isNull("description")) {
            return "";
        }
        Object description = data.get("description");
        try {
            return data.getString("description");
        } catch (JSONException e) {
            JSONArray stuff = data.getJSONObject("description").getJSONArray("extra");
            StringBuilder string = new StringBuilder();
            for (int i = 0; i < stuff.length(); i++) {
                JSONObject object = stuff.getJSONObject(i);
                String text = object.getString("text");
                if (object.has("strikethrough"))
                    text = "~~" + text + "~~";
                if (object.has("underline"))
                    text = "__" + text + "__";
                if (object.has("italic"))
                    text = "_" + text + "_";
                if (object.has("bold"))
                    text = "**" + text + "**";
                string.append(text);
            }
            String response = string.toString();
            response = response.replace("****", "").replace("~~~~", "");
            return response;
        }
    }
}
