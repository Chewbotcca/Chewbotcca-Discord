/*
 * Copyright (C) 2022 Chewbotcca
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
package pw.chew.chewbotcca.commands.util;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import pw.chew.chewbotcca.util.Mention;

import java.util.Collections;

// %^quote
public class QuoteCommand extends SlashCommand {

    public QuoteCommand() {
        this.name = "quote";
        this.help = "References a message from a server/channel this bot is in and can view";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "message_link", "The link (Copy URL) of a message to reference").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Message message;
        String[] link = event.optString("message_link", "").split("/");
        try {
            message = retrieveMessageFromLink(link, event.getJDA(), event.getChannelType() == ChannelType.TEXT ? event.getTextChannel() : null);
        } catch (IllegalArgumentException e) {
            event.reply("Could not get message! " + e.getMessage()).setEphemeral(true).queue();
            return;
        }

        boolean thisGuild = event.getGuild() != null && event.getGuild().getId().equals(message.getGuild().getId());
        boolean thisChannel = event.getChannel().getId().equals(message.getChannel().getId());
        event.replyEmbeds(gatherData(message, thisGuild, thisChannel, true)).addEmbeds(message.getEmbeds()).queue();
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

        boolean thisGuild = event.getMessage().isFromGuild() && event.getGuild().getId().equals(message.getGuild().getId());
        boolean thisChannel = event.getChannel().getId().equals(message.getChannel().getId());
        event.reply(gatherData(message, thisGuild, thisChannel, false));
    }

    /**
     * Builds a quote embed based on the message
     *
     * @param message The message to build the embed from
     * @param thisGuild Whether the message is from the same guild as the command
     * @param thisChannel Whether the message is from the same channel as the command
     * @param isFromSlash Whether the command is from a slash command
     * @return The embed
     */
    public static MessageEmbed gatherData(Message message, boolean thisGuild, boolean thisChannel, boolean isFromSlash) {
        // Get message details and send
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Quote");
        String content = message.getContentRaw();
        if (isFromSlash) {
            if (content.isBlank() && !message.getEmbeds().isEmpty()) {
                embed.setDescription("*This message contained only embed(s), see them below*");
            } else {
                embed.setDescription(content);
            }
        } else {
            if (content.isBlank() && !message.getEmbeds().isEmpty()) {
                embed.setDescription("*This message contained only embed(s), and they cannot be displayed. Please use /quote if possible*");
            } else {
                embed.setDescription(content);
            }
        }
        embed.setTimestamp(message.getTimeCreated());
        embed.setAuthor(message.getAuthor().getAsTag(), null, message.getAuthor().getAvatarUrl());
        if (message.isFromGuild()) {
            if (!thisGuild) {
                embed.addField("Server", message.getGuild().getName(), true);
            }
            if (!thisChannel && !thisGuild) {
                embed.addField("Channel", message.getChannel().getName(), true);
            } else if (!thisChannel) {
                embed.addField("Channel", message.getChannel().getAsMention(), true);
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

        return embed.build();
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
                try {
                    return retrieveMessageFromLink(link, event.getJDA(), event.getTextChannel());
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

    /**
     * Retrieves a message based on jump URL. Also has some sanity checks.
     *
     * @param link   The split, delimited by slashes
     * @param jda    The JDA instance
     * @param source The source of the interaction, null if it doesn't matter
     * @return A retrieved message
     * @throws IllegalArgumentException If an error occurred retrieving the message, or a check failed
     */
    public static Message retrieveMessageFromLink(String[] link, JDA jda, TextChannel source) {
        if (link.length != 7) {
            throw new IllegalArgumentException("Invalid link provided!");
        }

        MessageChannel channel;
        String serverId = link[4];
        String channelId = link[5];
        String messageId = link[6];
        if (serverId.equals("@me")) {
            channel = jda.getPrivateChannelById(channelId);
        } else {
            channel = jda.getTextChannelById(channelId);
        }

        if (channel == null)
            throw new IllegalArgumentException("Channel was invalid!");

        try {
            Message message = channel.retrieveMessageById(messageId).complete();
            // NSFW check
            if (source != null && message.isFromType(ChannelType.TEXT)) {
                if (!source.isNSFW() && message.getTextChannel().isNSFW()) {
                    throw new IllegalArgumentException("Messages from NSFW channels cannot be quoted in non-NSFW channels!");
                }
            }
            return message;
        } catch (NullPointerException | MissingAccessException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Message ID doesn't exist, is invalid, or inaccessible!");
        }
    }
}
