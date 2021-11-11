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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;
import pw.chew.jdachewtils.command.OptionHelper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

// %^mcuser command
public class MCUserCommand extends SlashCommand {

    public MCUserCommand() {
        this.name = "mcuser";
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
        String name = OptionHelper.optString(event, "user", "");
        try {
            event.replyEmbeds(gatherData(name)).queue();
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Get username/uuid from args
        String name = commandEvent.getArgs().split(" ")[0].replace("-", "");
        try {
            commandEvent.reply(gatherData(name));
        } catch (IllegalArgumentException e) {
            commandEvent.replyWarning(e.getMessage());
        }
    }

    private MessageEmbed gatherData(String name) {
        JSONArray history;
        String uuid;
        // Get profile info
        if (name.length() == 32) {
            // If it's a UUID
            try {
                history = new JSONArray(RestClient.get("https://api.mojang.com/user/profiles/" + name + "/names"));
                uuid = name;
            } catch (JSONException e) {
                throw new IllegalArgumentException("Not a valid input! Please enter a valid UUID!");
            }
        } else if (name.length() >= 1 && name.length() <= 16) {
            // If it's a username
            try {
                JSONObject profile = new JSONObject(RestClient.get("https://api.mojang.com/users/profiles/minecraft/" + name));
                history = new JSONArray(RestClient.get("https://api.mojang.com/user/profiles/" + profile.getString("id") + "/names"));
                uuid = profile.getString("id");
            } catch (JSONException e) {
                throw new IllegalArgumentException("Not a valid input! Please enter a valid UUID!");
            }
        } else {
            throw new IllegalArgumentException("Not a valid input! Please enter a valid username or a valid UUID!");
        }
        // Find recent names and when they were changed
        StringBuilder names = new StringBuilder();
        for(int i = history.length() - 1; i >= 0; i--) {
            JSONObject data = history.getJSONObject(i);
            String time;
            if(data.has("changedToAt")) {
                OffsetDateTime at = Instant.ofEpochMilli(data.getLong("changedToAt")).atOffset(ZoneOffset.UTC);
                time = TimeFormat.DATE_TIME_SHORT.format(at);
            } else {
                time = "Original";
            }
            String username = data.getString("name");
            names.append(time).append(" - `").append(username).append("`\n");
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