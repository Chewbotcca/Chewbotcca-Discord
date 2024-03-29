/*
 * Copyright (C) 2024 Chewbotcca
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

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.MiscUtil;
import pw.chew.chewbotcca.util.RestClient;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

// %^mcuser command
public class MCUserSubCommand extends SlashCommand {

    public MCUserSubCommand() {
        this.name = "user";
        this.help = "Looks up a Minecraft user and returns their profile";
        this.aliases = new String[]{"namemc"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "user", "The user name or UUID to lookup").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String name = event.optString("user", "");
        try {
            event.deferReply().queue(interactionHook -> interactionHook.editOriginalEmbeds(gatherData(name)).queue());
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    private MessageEmbed gatherData(String name) {
        String uuid;
        // Get profile info
        if (name.length() == 32 || name.length() == 36) {
            // If it's a UUID
            try {
                uuid = name;
            } catch (JSONException e) {
                throw new IllegalArgumentException("Not a valid input! Please enter a valid UUID!");
            }
        } else if (name.length() >= 1 && name.length() <= 16) {
            // If it's a username
            try {
                JSONObject profile = RestClient.get("https://api.mojang.com/users/profiles/minecraft/" + name).asJSONObject();
                uuid = profile.getString("id");
            } catch (JSONException e) {
                throw new IllegalArgumentException("Not a valid input! Please enter a valid username!");
            }
        } else {
            throw new IllegalArgumentException("Not a valid input! Please enter a valid username or a valid UUID!");
        }
        JSONObject profile = RestClient.get("https://laby.net/api/v2/user/" + uuid + "/get-profile").asJSONObject();
        JSONArray history = profile.getJSONArray("username_history");
        // Find recent names and when they were changed
        StringBuilder names = new StringBuilder();
        for(int i = history.length() - 1; i >= 0; i--) {
            JSONObject data = history.getJSONObject(i);
            String time;
            if(!data.isNull("changed_at")) {
                OffsetDateTime at = MiscUtil.dateParser(data.getString("changed_at"), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                time = TimeFormat.DATE_TIME_SHORT.format(at);
            } else {
                time = "Original";
            }
            String username = data.getString("name");
            names.append("`").append(username).append("` - ").append(time).append("\n");
        }
        // Return info
        return (new EmbedBuilder()
            .setTitle("Minecraft Profile Information for " + history.getJSONObject(history.length() - 1).getString("name"))
            .setDescription("[NameMC Profile](https://namemc.com/profile/" + uuid + ")")
            .setThumbnail("https://minotar.net/helm/" + uuid)
            .addField("Name History", names.toString(), true)
            .build());
    }
}
