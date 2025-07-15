/*
 * Copyright (C) 2025 Chewbotcca
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
package pw.chew.chewbotcca.commands.info;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import pw.chew.chewbotcca.util.EmojiUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h2>ChannelInfo Command</h2>
 *
 * <a href="https://help.chew.pro/bots/discord/chewbotcca/commands/channelinfo">Docs</a>
 */
public class ChannelInfoCommand extends SlashCommand {
    public ChannelInfoCommand() {
        this.name = "channelinfo";
        this.help = "Gathers info about a specified channel";
        this.aliases = new String[]{"cinfo"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD};
        this.options = Arrays.asList(
            new OptionData(OptionType.CHANNEL, "channel", "The channel to find info about", false),
            new OptionData(OptionType.STRING, "type", "The type of information to return. default: general")
                .addChoices(
                    new Command.Choice("General Information", "general"),
                    new Command.Choice("Pins", "pins")
                )
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        GuildChannel channel = event.optGuildChannel("channel", event.getGuildChannel());
        String type = event.optString("type", "general");

        if (type.equals("general")) {
            event.replyEmbeds(gatherMainInfo(channel).build()).queue();
        } else {
            if (channel.getType() != ChannelType.TEXT) {
                event.reply("Pins can only be retrieved from text channels!").setEphemeral(true).queue();
                return;
            }
            event.replyEmbeds(getPinsInfo((TextChannel) channel, event.getJDA()).build()).queue();
        }
    }

    /**
     * Gather main info
     *
     * @param channel the channel
     * @return an embed ready to be built
     */
    public EmbedBuilder gatherMainInfo(GuildChannel channel) {
        // Start generating an embed
        EmbedBuilder e = new EmbedBuilder();
        // If it's a text channel, add a hashtag, no need for one otherwise
        if (channel.getType() == ChannelType.TEXT) {
            e.setTitle("Channel Info for #" + channel.getName());
        } else {
            e.setTitle("Channel Info for " + channel.getName());
        }

        e.addField("ID", channel.getId(), true);
        e.addField("Type", channel.getType().toString(), true);

        if (channel.getType() == ChannelType.VOICE) {
           e.setDescription(((VoiceChannel) channel).getStatus());
        }

        // Users in channel if it's an audio channel.
        if (channel.getType().isAudio()) {
            AudioChannel vc = ((AudioChannel) channel);
            e.addField("Users in Channel", String.valueOf(vc.getMembers().size()), true);
            if (vc.getRegion() == Region.AUTOMATIC) {
                e.addField("Voice Region", "Automatic", true);
            } else {
                e.addField("Voice Region", vc.getRegion().getEmoji() + " " + vc.getRegion().getName(), true);
            }
        }

        // If it's a text channel, and we can access the webhooks, add the count.
        if (channel.getType() == ChannelType.TEXT) {
            TextChannel textChannel = ((TextChannel) channel);
            e.setDescription(textChannel.getTopic());
            if (channel.getGuild().getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
                List<Webhook> hooks = textChannel.retrieveWebhooks().complete();
                e.addField("Webhooks", String.valueOf(hooks.size()), true);
            }
            if (channel.getGuild().getSelfMember().hasPermission(Permission.VIEW_CHANNEL)) {
                e.addField("Pins", textChannel.retrievePinnedMessages().complete().size() + " / 50", true);
            }
            List<String> info = new ArrayList<>();
            if (channel.getType() == ChannelType.NEWS)
                info.add(EmojiUtil.Emoji.NEWS.mention() + " News");
            if (textChannel.isNSFW())
                info.add(EmojiUtil.Emoji.CHANNEL_NSFW.mention() + " Age-Restricted");
            if (!info.isEmpty())
                e.addField("Information", String.join("\n", info), true);
        }

        e.setFooter("Channel Created");
        e.setTimestamp(channel.getTimeCreated());

        return e;
    }

    /**
     * Get pins info for a channel
     *
     * @param channel the channel
     * @param jda     jda for getting users
     * @return an embed ready to be build
     */
    public EmbedBuilder getPinsInfo(TextChannel channel, JDA jda) {
        EmbedBuilder e = new EmbedBuilder();
        // Retrieve the channel pins
        List<Message> pins = channel.retrievePinnedMessages().complete();
        // Find the top pins users and sort it
        HashMap<String, Integer> topPins = new HashMap<>();
        for (Message message : pins) {
            String authorId = message.getAuthor().getId();
            int current = topPins.getOrDefault(authorId, 0);
            topPins.put(authorId, current + 1);
        }
        ArrayList<Map.Entry<String, Integer>> l = new ArrayList<>(topPins.entrySet());
        l.sort(Map.Entry.comparingByValue());
        // Have to reverse since it's in 1, 2, 3 order. Ascending I think it's called
        Collections.reverse(l);
        // Make an arraylist with the "#x: User - Pins"
        List<CharSequence> top = new ArrayList<>();
        top.add("Total Pins: " + pins.size() + " / 250");
        for (int i = 0; i < l.size(); i++) {
            Map.Entry<String, Integer> entry = l.get(i);
            String user = entry.getKey();
            int pinCount = entry.getValue();
            User userById = jda.getUserById(user);
            String tag;
            if (userById == null) {
                try {
                    tag = jda.retrieveUserById(user).complete().getAsTag();
                } catch (ErrorResponseException ignored) {
                    tag = "Unknown User";
                }
            } else {
                if (userById.isBot()) {
                    tag = userById.getAsTag();
                } else {
                    tag = userById.getName();
                }
            }
            top.add((i + 1) + ". " + pinCount + " pins - " + tag);
        }
        e.setDescription(String.join("\n", top));
        return e;
    }
}
