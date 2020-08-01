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
package pw.chew.chewbotcca.commands.quotes;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;

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
        String[] args = event.getArgs().split(" ");
        // Get the message ID (first arg)
        String mesId = args[0];
        AtomicReference<Message> message = new AtomicReference<>();
        if(args.length == 1) {
            // Retrieve the message
            event.getChannel().retrieveMessageById(mesId).queue(message::set,
                    (exception) -> event.reply("Invalid Message ID. If this message is in a separate channel, provide its ID as well.")
            );
            await().atMost(5, TimeUnit.SECONDS).until(() -> message.get() != null);
        } else {
            // Get the second (channel id) arg
            String chanId = args[1];
            chanId = chanId.replace("<#", "").replace(">", "");
            TextChannel channel;
            // Get the text channel
            channel = event.getJDA().getTextChannelById(chanId);
            // If it's not null
            if (channel != null) {
                channel.retrieveMessageById(mesId).queue(message::set,
                        (exception) -> event.reply("Invalid Message ID. That message might not exist in the channel provided.")
                );
                await().atMost(5, TimeUnit.SECONDS).until(() -> message.get() != null);
            } else {
                event.reply("Invalid Channel ID.");
                return;
            }
        }

        if(message.get() == null) {
            return;
        }

        // Get message details and send
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Quote");
        embed.setDescription(message.get().getContentRaw());
        embed.setTimestamp(message.get().getTimeCreated());
        embed.setAuthor(message.get().getAuthor().getAsTag(), null, message.get().getAuthor().getAvatarUrl());
        boolean thisGuild = event.getGuild() == message.get().getGuild();
        if(!thisGuild)
            embed.addField("Server", message.get().getGuild().getName(), true);
        if(args.length > 1 && !thisGuild) {
            embed.addField("Channel", message.get().getChannel().getName(), true);
        } else if (args.length > 1) {
            embed.addField("Channel", ((TextChannel) message.get().getChannel()).getAsMention(), true);
        }
        embed.addField("Jump", "[Link](" + message.get().getJumpUrl() + ")", true);
        AtomicReference<Member> member = new AtomicReference<>();
        AtomicBoolean bruh = new AtomicBoolean(false);
        event.getGuild().retrieveMember(message.get().getAuthor()).queue(member::set, (oh) -> bruh.set(true));
        await().atMost(5, TimeUnit.SECONDS).until(() -> member.get() != null || bruh.get());

        if(!bruh.get())
            embed.setColor(member.get().getColor());
        event.reply(embed.build());
    }
}
