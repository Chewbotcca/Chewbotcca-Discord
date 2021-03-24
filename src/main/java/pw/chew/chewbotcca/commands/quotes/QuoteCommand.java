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
package pw.chew.chewbotcca.commands.quotes;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import pw.chew.chewbotcca.util.Mention;

// %^quote
public class QuoteCommand extends Command {

    public QuoteCommand() {
        this.name = "quote";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        // Start typing
        event.getChannel().sendTyping().queue();

        Message message;
        try {
            message = retrieveMessage(event);
        } catch (IllegalArgumentException e) {
            event.reply("Could not get message! " + e.getMessage());
            return;
        }

        // Get message details and send
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Quote");
        embed.setDescription(message.getContentRaw());
        embed.setTimestamp(message.getTimeCreated());
        embed.setAuthor(message.getAuthor().getAsTag(), null, message.getAuthor().getAvatarUrl());
        if (message.isFromGuild()) {
            boolean thisGuild = event.getMessage().isFromGuild() && event.getGuild().getId().equals(message.getGuild().getId());
            boolean thisChannel = event.getChannel().getId().equals(message.getChannel().getId());
            if (!thisGuild) {
                embed.addField("Server", message.getGuild().getName(), true);
            }
            if (!thisChannel && !thisGuild) {
                embed.addField("Channel", message.getChannel().getName(), true);
            } else if (!thisChannel) {
                embed.addField("Channel", ((TextChannel) message.getChannel()).getAsMention(), true);
            }
            try {
                Member member = message.getGuild().retrieveMember(message.getAuthor()).complete();
                embed.setColor(member.getColor());
            } catch (ErrorResponseException ignored) {
            }
            embed.addField("Jump", "[Link](" + message.getJumpUrl() + ")", true);
        } else {
            embed.addField("Channel", "DMs with " + message.getPrivateChannel().getUser().getAsTag(), true);
        }

        event.reply(embed.build());
    }

    private Message retrieveMessage(CommandEvent event) {
        String arg = event.getArgs();
        String[] args = arg.split(" ");
        MessageChannel channel;
        if (event.getChannelType() == ChannelType.PRIVATE) {
            channel = event.getPrivateChannel();
        } else {
            channel = event.getTextChannel();
        }

        // If one argument is provided
        if (args.length == 1) {
            // Check if it's a link
            String[] link = arg.split("/");
            if (link.length == 7) {
                String serverId = link[4];
                String channelId = link[5];
                String messageId = link[6];
                if (serverId.equals("@me")) {
                    channel = event.getJDA().getPrivateChannelById(channelId);
                } else {
                    channel = event.getJDA().getTextChannelById(channelId);
                }

                if (channel == null)
                    throw new IllegalArgumentException("Channel was invalid!");

                try {
                    return channel.retrieveMessageById(messageId).complete();
                } catch (NullPointerException | IllegalArgumentException e) {
                    throw new IllegalArgumentException("Message ID doesn't exist or is invalid!");
                }
            }

            return channel.retrieveMessageById(arg).complete();
        }

        // Assuming channel then message ID
        if (args.length == 2) {
            try {
                channel = (TextChannel) Mention.parseMention(args[0], event.getGuild(), event.getJDA());
                if (channel == null) {
                    throw new IllegalArgumentException("Channel does not exist!");
                }
                return channel.retrieveMessageById(args[1]).complete();
            } catch (ClassCastException | NullPointerException | IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid or illegal Channel/Message ID provided!");
            }
        }

        throw new IllegalArgumentException("Too many arguments provided!");
    }
}
