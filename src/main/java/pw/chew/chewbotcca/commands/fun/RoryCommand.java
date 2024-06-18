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
package pw.chew.chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.CooldownScope;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.WebhookType;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class RoryCommand extends SlashCommand {

    public RoryCommand() {
        this.name = "rory";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL};
        this.children = new SlashCommand[]{new GetRorySubCommand(), new FollowRorySubCommand()};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Unsupported for slash commands with children with options
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getChannel().sendTyping().queue();
        JSONObject rory = RestClient.get("https://rory.cat/purr/" + event.getArgs()).asJSONObject();
        if (rory.has("error")) {
            event.reply(rory.getString("error"));
            return;
        }

        EmbedBuilder embed = generateRoryEmbed(rory);
        if (event.getChannelType() == ChannelType.TEXT && event.getMember().hasPermission(Permission.MANAGE_WEBHOOKS) && event.getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            embed.setDescription("Stay up to date with new Rory images by running `" + event.getPrefix() + "rory follow`!");
        }

        event.reply(embed.build());
    }

    private EmbedBuilder generateRoryEmbed(JSONObject rory) {
        String permalink = "https://rory.cat/id/" + rory.getInt("id");

        return new EmbedBuilder()
            .setTitle("Rory :3", permalink)
            .setImage(rory.getString("url") + "?nocache" + Instant.now().getEpochSecond())
            .setFooter("ID: " + rory.getInt("id"));
    }

    public class GetRorySubCommand extends SlashCommand {
        public GetRorySubCommand() {
            this.name = "get";
            this.help = "Gets a Rory photo!";
            this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL};
            this.options = Collections.singletonList(
                new OptionData(OptionType.INTEGER, "id", "The ID of a rory! Leave blank for a random Rory")
            );
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            JSONObject rory = RestClient.get("https://rory.cat/purr/" + event.optString("id", "")).asJSONObject();
            if (rory.has("error")) {
                event.reply(rory.getString("error")).setEphemeral(true).queue();
                return;
            }

            EmbedBuilder embed = generateRoryEmbed(rory);
            if (event.getChannelType() == ChannelType.TEXT && !event.getGuild().isDetached() && event.getMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
                embed.setDescription("Stay up to date with new Rory images by running `/rory follow`!");
            }

            event.replyEmbeds(embed.build()).queue();
        }
    }

    public static class FollowRorySubCommand extends SlashCommand {
        public FollowRorySubCommand() {
            this.name = "follow";
            this.help = "Follows the Rory Image feed to the current channel (Requires Manage Webhooks)";
            this.botPermissions = new Permission[]{Permission.MANAGE_WEBHOOKS};
            this.userPermissions = new Permission[]{Permission.MANAGE_WEBHOOKS};
            this.contexts = new InteractionContextType[]{InteractionContextType.GUILD};
            this.cooldown = 30;
            this.cooldownScope = CooldownScope.CHANNEL;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            TextChannel destination = event.getTextChannel();
            if (event.getChannelType() == ChannelType.NEWS) {
                event.reply("News channels cannot be followed to other news channels!").setEphemeral(true).queue();
                return;
            }
            if (alreadyFollowing(destination)) {
                event.reply("This channel already has a Rory Images feed! (If not, an error occurred, oopsie)").setEphemeral(true).queue();
                return;
            }
            NewsChannel rory = event.getJDA().getNewsChannelById("752063016425619487");
            if (rory == null) {
                event.reply("Rory channel not found! :(").setEphemeral(true).queue();
                return;
            }
            rory.follow(destination).queue(yay -> event.reply("Followed the rory images channel successfully, enjoy the Rory :3").queue());
        }

        @Override
        protected void execute(CommandEvent event) {
            TextChannel destination = event.getTextChannel();
            if (event.isFromType(ChannelType.NEWS)) {
                event.reply("News channels cannot be followed to other news channels!");
                return;
            }
            if (alreadyFollowing(destination)) {
                event.reply("This channel already has a Rory Images feed! (If not, an error occurred, oopsie)");
                return;
            }
            NewsChannel rory = event.getJDA().getNewsChannelById("752063016425619487");
            if (rory == null) {
                event.reply("Rory channel not found! :(");
                return;
            }
            rory.follow(destination).queue(yay -> event.reply("Followed the rory images channel successfully, enjoy the Rory :3"));
        }

        public boolean alreadyFollowing(TextChannel channel) {
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
