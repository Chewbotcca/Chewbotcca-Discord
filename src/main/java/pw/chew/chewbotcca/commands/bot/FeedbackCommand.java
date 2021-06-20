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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import pw.chew.chewbotcca.util.ResponseHelper;

import java.awt.Color;
import java.time.Instant;
import java.util.Collections;

// %^feedback command
public class FeedbackCommand extends SlashCommand {

    public FeedbackCommand() {
        this.name = "feedback";
        this.help = "Leave some feedback about the bot";
        this.cooldown = 30;
        this.cooldownScope = CooldownScope.USER;
        this.guildOnly = false;
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "feedback", "The feedback you want to leave").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        try {
            var feedback = event.getOption("feedback").getAsString();
            TextChannel feedbackChannel = retrieveFeedbackChannel(event.getJDA());
            feedbackChannel.sendMessage(generateFeedbackEmbed(feedback, event.getUser())).queue(
                message -> event.reply("I have successfully sent the feedback! Feel free to see it on the help server with `/invite`")
                    .setEphemeral(true)
                    .queue()
            );
        } catch (IllegalArgumentException e) {
            event.replyEmbeds(ResponseHelper.generateFailureEmbed(null, e.getMessage())).setEphemeral(true).queue();
        }

    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        try {
            var feedback = commandEvent.getArgs();
            TextChannel feedbackChannel = retrieveFeedbackChannel(commandEvent.getJDA());
            feedbackChannel.sendMessage(generateFeedbackEmbed(feedback, commandEvent.getAuthor())).queue(
                message -> commandEvent.reply("I have successfully sent the feedback! Feel free to see it on the help server with `" + commandEvent.getPrefix() + "invite`")
            );
        } catch (IllegalArgumentException e) {
            commandEvent.replyError(e.getMessage());
        }
    }

    private MessageEmbed generateFeedbackEmbed(String feedback, User author) {
        if (feedback.length() < 10) {
            throw new IllegalArgumentException("Your feedback is too short, how are we supposed to improve! Please enter at least 10 characters.");
        }
        var embed = new EmbedBuilder();
        embed.setTitle("New Feedback!");
        embed.setColor(Color.decode("#6166A8"));
        embed.setDescription(feedback);
        embed.setTimestamp(Instant.now());
        embed.setAuthor(author.getAsTag(), null, author.getAvatarUrl());
        embed.setFooter("User ID: " + author.getId());
        return embed.build();
    }

    private TextChannel retrieveFeedbackChannel(JDA jda) {
        TextChannel feedbackChannel = jda.getTextChannelById("745164378659225651");
        if (feedbackChannel == null) {
            throw new IllegalArgumentException("Could not find feedback channel!");
        }
        return feedbackChannel;
    }
}