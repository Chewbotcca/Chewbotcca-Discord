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
package pw.chew.chewbotcca.commands.bot;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.time.Instant;
import java.util.Objects;

// %^feedback command
public class FeedbackCommand extends Command {

    public FeedbackCommand() {
        this.name = "feedback";
        this.cooldown = 30;
        this.cooldownScope = CooldownScope.USER;
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Get args
        var feedback = commandEvent.getArgs();
        if (feedback.length() < 10) {
            commandEvent.reply("Your feedback is too short, how are we supposed to improve! Please enter at least 10 characters.");
            return;
        }
        var embed = new EmbedBuilder();
        embed.setTitle("New Feedback!");
        embed.setColor(Color.decode("#6166A8"));
        embed.setDescription(feedback);
        embed.setTimestamp(Instant.now());
        embed.setAuthor(commandEvent.getAuthor().getAsTag(), null, commandEvent.getAuthor().getAvatarUrl());
        embed.setFooter("User ID: " + commandEvent.getAuthor().getId());
        // Get the feedback channel and send
        Objects.requireNonNull(commandEvent.getJDA().getTextChannelById("745164378659225651")).sendMessage(embed.build()).queue(
            message -> commandEvent.reply("I have successfully sent the feedback! Feel free to see it on the help server with `" + commandEvent.getPrefix() + "invite`")
        );
    }
}