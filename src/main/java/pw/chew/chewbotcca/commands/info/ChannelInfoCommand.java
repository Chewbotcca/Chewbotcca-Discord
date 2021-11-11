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
package pw.chew.chewbotcca.commands.info;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import pw.chew.jdachewtils.command.OptionHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// %^channelinfo command
public class ChannelInfoCommand extends SlashCommand {
    public ChannelInfoCommand() {
        this.name = "channelinfo";
        this.help = "Gathers info about a specified channel";
        this.aliases = new String[]{"cinfo"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = true;
        this.options = Arrays.asList(
            new OptionData(OptionType.CHANNEL, "channel", "The channel to find info about").setRequired(true),
            new OptionData(OptionType.STRING, "type", "The type of information to return. default: general")
                .addChoices(
                    new Command.Choice("General Information", "general"),
                    new Command.Choice("Pins", "pins")
                )
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        GuildChannel channel = OptionHelper.optGuildChannel(event, "channel", event.getGuildChannel());
        event.replyEmbeds(switch (OptionHelper.optString(event, "type", "general")) {
            case "pins" -> getPinsInfo((TextChannel) channel, event.getJDA()).build();
            default -> gatherMainInfo(channel, null).build();
        }).queue();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Get args
        String args = commandEvent.getArgs();
        String mode = "";

        // Set the mode to pins if requested
        if(args.contains("pins")) {
            mode = "pins";
            args = args.replace("--pins", "").trim();
        }

        // Find the channel based on input if possible
        GuildChannel channel;
        if(args.length() == 0) {
            channel = commandEvent.getGuild().getGuildChannelById(commandEvent.getChannel().getId());
        } else if(args.contains("<#")) {
            String id = args.replace("<#", "").replace(">", "");
            channel = commandEvent.getGuild().getGuildChannelById(id);
        } else {
            try {
                channel = commandEvent.getGuild().getGuildChannelById(args);
            } catch(NumberFormatException e) {
                commandEvent.reply("This channel could not be found!");
                return;
            }
        }

        // If there's no channel
        if(channel == null) {
            commandEvent.reply("This channel could not be found!");
            return;
        }

        // Generate and send data based on input
        if(mode.equals("pins") && channel.getType() == ChannelType.TEXT) {
            commandEvent.reply(getPinsInfo((TextChannel)channel, commandEvent.getJDA()).build());
        } else if(mode.equals("pins")) {
            commandEvent.reply("Pins sub-command only works in Text channels!");
        } else {
            commandEvent.reply(gatherMainInfo(channel, commandEvent).build());
        }
    }

    /**
     * Gather main info
     * @param channel the channel
     * @param commandEvent the command event
     * @return an embed ready to be built
     */
    public EmbedBuilder gatherMainInfo(GuildChannel channel, CommandEvent commandEvent) {
        // Start generating an embed
        EmbedBuilder e = new EmbedBuilder();
        // If it's a text channel, add a hashtag, no need for one otherwise
        if(channel.getType() == ChannelType.TEXT) {
            e.setTitle("Channel Info for #" + channel.getName());
            e.setDescription(((TextChannel)channel).getTopic());
        } else {
            e.setTitle("Channel Info for " + channel.getName());
        }

        e.addField("ID", channel.getId(), true);

        // Users in channel if it's a text or voice.
        // If it's text, it's users who can see the channel.
        // If it's voice, it's users actually in the channel.
        if (channel.getType() == ChannelType.TEXT || channel.getType() == ChannelType.VOICE)
            e.addField("Users in Channel", String.valueOf(channel.getMembers().size()), true);

        if (channel.getType() == ChannelType.VOICE) {
            VoiceChannel vc = ((VoiceChannel) channel);
            e.addField("Voice Region", vc.getRegion().getEmoji() + " " + vc.getRegion().getName(), true);
        }

        e.addField("Type", channel.getType().toString(), true);

        // If it's a text channel and we can access the webhooks, add the count.
        if (channel.getType() == ChannelType.TEXT) {
            TextChannel textChannel = ((TextChannel) channel);
            if (commandEvent != null && commandEvent.getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
                List<Webhook> hooks = textChannel.retrieveWebhooks().complete();
                e.addField("Webhooks", String.valueOf(hooks.size()), true);
            }
            if (commandEvent != null && commandEvent.getSelfMember().hasPermission(Permission.VIEW_CHANNEL)) {
                e.addField("Pins", textChannel.retrievePinnedMessages().complete().size() + " / 50", true);
            }
            List<String> info = new ArrayList<>();
            if (textChannel.isNews())
                info.add("<:news:725504846937063595> News");
            if (textChannel.isNSFW())
                info.add("<:channel_nsfw:585783907660857354> NSFW");
            if (!info.isEmpty())
                e.addField("Information", String.join("\n", info), true);
        }

        e.setFooter("Channel Created");
        e.setTimestamp(channel.getTimeCreated());

        return e;
    }

    /**
     * Get pins info for a channel
     * @param channel the channel
     * @param jda jda for getting users
     * @return an embed ready to be build
     */
    public EmbedBuilder getPinsInfo(TextChannel channel, JDA jda) {
        EmbedBuilder e = new EmbedBuilder();
        // Retrieve the channel pins
        List<Message> pins = channel.retrievePinnedMessages().complete();
        // Find the top pins users and sort it
        HashMap<String, Integer> topPins = new HashMap<>();
        for(Message message : pins) {
            String authorId = message.getAuthor().getId();
            int current = topPins.getOrDefault(authorId, 0);
            topPins.put(authorId, current + 1);
        }
        ArrayList<Map.Entry<String, Integer>> l = new ArrayList<>(topPins.entrySet());
        l.sort(Map.Entry.comparingByValue());
        // Have to reverse since it's in 1, 2, 3 order. Ascending i think it's called
        Collections.reverse(l);
        // Make an arraylist with the "#x: User - Pins"
        List<CharSequence> top = new ArrayList<>();
        top.add("Total Pins: " + pins.size() + " / 50");
        for(int i = 0; i < l.size(); i++) {
            Map.Entry<String, Integer> entry = l.get(i);
            String user = entry.getKey();
            int pinCount = entry.getValue();
            User userById = jda.getUserById(user);
            String tag;
            if(userById == null) {
                try {
                    tag = jda.retrieveUserById(user).complete().getAsTag();
                } catch (ErrorResponseException ignored) {
                    tag = "Unknown User";
                }
            } else {
                tag = userById.getAsTag();
            }
            top.add("#" + (i+1) + ": " + pinCount + " pins - " + tag);
        }
        e.setDescription(String.join("\n", top));
        return e;
    }
}