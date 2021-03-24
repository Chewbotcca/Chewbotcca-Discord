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
package pw.chew.chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.WebhookType;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import java.util.List;
import java.io.IOException;
import java.time.Instant;

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
            .setImage(rory.getString("url") + "?nocache" + Instant.now().getEpochSecond())
            .setFooter("ID: " + rory.getInt("id"));
        if (event.getChannelType() == ChannelType.TEXT && event.getMember().hasPermission(Permission.MANAGE_WEBHOOKS) && event.getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            embed.setDescription("Stay up to date with new Rory images by running `" + event.getPrefix() + "rory follow`!");
        }

        event.reply(embed.build());
    }

    public static class FollowRorySubCommand extends Command {
        public FollowRorySubCommand() {
            this.name = "follow";
            this.botPermissions = new Permission[]{Permission.MANAGE_WEBHOOKS};
            this.userPermissions = new Permission[]{Permission.MANAGE_WEBHOOKS};
            this.guildOnly = true;
            this.cooldown = 30;
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
            TextChannel rory = event.getJDA().getTextChannelById("752063016425619487");
            if (rory == null) {
                event.reply("Rory channel not found! :(");
                return;
            }
            rory.follow(destination).queue(yay -> event.reply("Followed the rory images channel successfully, enjoy the Rory :3"));
        }

        public boolean alreadyFollowing(TextChannel channel, CommandEvent event) {
            List<Webhook> webhooks = channel.retrieveWebhooks().complete();
            for (Webhook webhook : webhooks) {
                if (webhook.getType() != WebhookType.FOLLOWER) {
                    continue;
                }
                Webhook.ChannelReference refChannel = webhook.getSourceChannel();
                if (refChannel != null && refChannel.getId().equals("752063016425619487")) {
                    return true;
                }
            }
            return false;
        }
    }
}
