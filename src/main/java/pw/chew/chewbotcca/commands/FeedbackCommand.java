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
import net.dv8tion.jda.api.entities.ChannelType;

import java.awt.*;
import java.time.Instant;
import java.util.Objects;

// %^feedback command
public class FeedbackCommand extends Command {

    public FeedbackCommand() {
        this.name = "feedback";
        this.cooldown = 30;
        this.cooldownScope = CooldownScope.USER;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Get args
        var feedback = commandEvent.getArgs();
        var embed = new EmbedBuilder();
        embed.setTitle("New Feedback!");
        embed.setColor(Color.decode("#6166A8"));
        embed.setDescription(feedback);
        embed.setTimestamp(Instant.now());
        embed.setAuthor(commandEvent.getAuthor().getAsTag(), null, commandEvent.getAuthor().getAvatarUrl());
        embed.addField("User ID", commandEvent.getAuthor().getId(), true);
        if(commandEvent.getChannelType() == ChannelType.PRIVATE) {
            embed.addField("Server", "Sent from a DM", true);
        } else {
            embed.addField("Server", "Name: " + commandEvent.getGuild().getName() + "\n" + commandEvent.getGuild().getId(), true);
        }
        // Get the feedback channel and send
        Objects.requireNonNull(commandEvent.getJDA().getTextChannelById("720118610785468446")).sendMessage(embed.build()).queue();
        commandEvent.reply("I have successfully sent the feedback! Feel free to see it on the help server with `%^invite`");
    }
}