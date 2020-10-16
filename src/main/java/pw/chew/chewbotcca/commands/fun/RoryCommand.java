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
package pw.chew.chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import pw.chew.chewbotcca.util.RestClient;

import java.io.IOException;

public class RoryCommand extends Command {

    public RoryCommand() {
        this.name = "rory";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.children = new Command[]{new FollowRorySubCommand()};
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getChannel().sendTyping().queue();
        JSONObject rory = new JSONObject(RestClient.get("https://rory.cat/purr/" + event.getArgs()));
        if (rory.has("error")) {
            event.reply(rory.getString("error"));
            return;
        }
        String permalink = "https://rory.cat/id/" + rory.getInt("id");

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("Rory :3", permalink)
            .setImage(rory.getString("url"))
            .setFooter("ID: " + rory.getInt("id"));
        if (event.getMember().hasPermission(Permission.MANAGE_WEBHOOKS) && event.getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            embed.setDescription("Stay up to date with new Rory images by running `%^rory follow`!");
        }

        event.reply(embed.build());
    }

    public static class FollowRorySubCommand extends Command {
        public FollowRorySubCommand() {
            this.name = "follow";
            this.botPermissions = new Permission[]{Permission.MANAGE_WEBHOOKS};
            this.userPermissions = new Permission[]{Permission.MANAGE_WEBHOOKS};
            this.guildOnly = true;
            this.cooldown = 1000;
            this.cooldownScope = CooldownScope.CHANNEL;
        }

        @Override
        protected void execute(CommandEvent event) {
            TextChannel destination = event.getTextChannel();
            // Not sure if I want to implement selecting another channel yet.
            /*
            if (!event.getArgs().isEmpty()) {
                Object parse = Mention.parseMention(event.getArgs(), event.getGuild(), event.getJDA());
                if (!(parse instanceof GuildChannel)) {
                    destination = event.getGuild().getTextChannelById(event.getArgs());
                } else {
                    destination = (TextChannel) parse;
                }
            }
            if (destination == null) {
                event.reply("Please provide a valid destination!");
                return;
            }
            */
            if (destination.isNews()) {
                event.reply("News channels cannot be followed to other news channels!");
                return;
            }
            if (alreadyFollowing(destination, event)) {
                event.reply("This channel already has a Rory Images feed! (If not, an error occurred, oopsie)");
                return;
            }
            MediaType JSON = MediaType.parse("application/json");
            OkHttpClient client = event.getJDA().getHttpClient();
            RequestBody body = RequestBody.create("{\"webhook_channel_id\": \"" + destination.getId() + "\"}", JSON);
            Request request = new Request.Builder()
                .url("https://discord.com/api/v6/channels/752063016425619487/followers")
                .addHeader("Authorization", event.getJDA().getToken())
                .post(body)
                .build();
            try (Response response = client.newCall(request).execute()) {
                ResponseBody responseBody = response.body();
                if (!response.isSuccessful()) {
                    // ... handle failed request
                    event.reply("Error occurred! Bug me with `%^feedback you done messed up with that %^rory follow command of yours`");
                    LoggerFactory.getLogger(this.getClass()).error(response.code() + " " + responseBody.string());
                    return;
                }
            } catch (IOException e) {
                // ... handle IO exception
                e.printStackTrace();
            }
            event.reply("Followed the rory images channel successfully, enjoy the Rory :3");
        }

        public boolean alreadyFollowing(TextChannel channel, CommandEvent event) {
            JSONArray response = new JSONArray(RestClient.get("https://discord.com/api/v6/channels/" + channel.getId() + "/webhooks", event.getJDA().getToken()));
            for (int i = 0; i < response.length(); i++) {
                JSONObject index = response.getJSONObject(i);
                if (index.getInt("type") == 1) {
                    continue;
                }
                JSONObject source = index.getJSONObject("source_channel");
                if (source.getString("id").equals("752063016425619487")) {
                    return true;
                }
            }
            return false;
        }
    }
}
