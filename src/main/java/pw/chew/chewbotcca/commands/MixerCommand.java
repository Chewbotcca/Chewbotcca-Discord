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
package pw.chew.chewbotcca.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import java.awt.*;

// %^mixer command
// Honestly not worth commenting, this command getting deleted soon anyway because rip mixer
public class MixerCommand extends Command {

    public MixerCommand() {
        this.name = "mixer";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        JSONObject parse;
        parse = new JSONObject(RestClient.get("https://mixer.com/api/v1/channels/" + event.getArgs()));
        if(parse.has("error")) {
            event.reply("User not found!");
            return;
        }
        String name = parse.getString("token");
        String online;
        if (parse.getBoolean("online")) {
           online = "Currently Streaming!";
        }  else{
            online = "Currently Offline";
        }
        int followers = parse.getInt("numFollowers");
        String avatar = parse.getJSONObject("user").getString("avatarUrl");

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Mixer info for user " + name, "http://mixer.com/" + name)
                .setDescription(online)
                .setThumbnail(avatar)
                .addField("Stream Title", parse.getString("name"), false)
                .addField("Followers", String.valueOf(followers), true)
                .addField("Total Views", String.valueOf(parse.getInt("viewersTotal")), true);
        if (parse.getBoolean("online")) {
            embed.setColor(Color.decode("#43B581"));
        } else {
            embed.setColor(Color.decode("#FAA61A"));
        }

        event.reply(embed.build());
    }
}
