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

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;

// %^channelinfo command
public class ChannelInfoCommand extends Command {
    public ChannelInfoCommand() {
        this.name = "channelinfo";
        this.aliases = new String[]{"cinfo"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
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
            commandEvent.reply(getPinsInfo((TextChannel)channel, commandEvent).build());
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

        e.addField("Type", channel.getType().toString(), true);

        // If it's a text channel and we can access the webhooks, add the count.
        if(channel.getType() == ChannelType.TEXT && commandEvent.getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            AtomicReference<List<Webhook>> hooks = new AtomicReference<>();
            ((TextChannel) channel).retrieveWebhooks().queue((hooks::set));
            await().atMost(5, TimeUnit.SECONDS).until(() -> hooks.get() != null);
            e.addField("Webhooks", String.valueOf(hooks.get().size()), true);
        }

        e.setFooter("Channel Created");
        e.setTimestamp(channel.getTimeCreated());

        return e;
    }

    /**
     * Get pins info for a channel
     * @param channel the channel
     * @param commandEvent the command event
     * @return an embed ready to be build
     */
    public EmbedBuilder getPinsInfo(TextChannel channel, CommandEvent commandEvent) {
        EmbedBuilder e = new EmbedBuilder();
        // Retrieve the channel pins
        AtomicReference<List<Message>> pinAR = new AtomicReference<>();
        channel.retrievePinnedMessages().queue((pinAR::set));
        // I absolutely hate async, and I'm not used to it, this puts it back in sync
        await().atMost(5, TimeUnit.SECONDS).until(() -> pinAR.get() != null);
        List<Message> pins = pinAR.get();
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
            User userById = commandEvent.getJDA().getUserById(user);
            AtomicReference<String> tag = new AtomicReference<>();
            if(userById == null) {
                commandEvent.getJDA().retrieveUserById(user).queue(bruh -> tag.set(bruh.getAsTag()), error -> tag.set("Deleted User"));
            } else {
                tag.set(userById.getAsTag());
            }
            await().atMost(5, TimeUnit.SECONDS).until(() -> tag.get() != null);
            top.add("#" + (i+1) + ": " + pinCount + " pins - " + tag.get());
        }
        e.setDescription(String.join("\n", top));
        return e;
    }
}