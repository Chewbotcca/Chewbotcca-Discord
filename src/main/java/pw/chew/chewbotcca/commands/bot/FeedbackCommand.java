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
package pw.chew.chewbotcca.commands.bot;

import com.jagrosh.jdautilities.command.CooldownScope;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import pw.chew.chewbotcca.util.ResponseHelper;

import java.time.Instant;
import java.util.Collections;

/**
 * <h2><code>/feedback</code> Command</h2>
 *
 * <a href="https://help.chew.pro/bots/discord/chewbotcca/commands/feedback">Docs</a>
 */
public class FeedbackCommand extends SlashCommand {
    public FeedbackCommand() {
        this.name = "feedback";
        this.help = "Leave some feedback about the bot";
        this.cooldown = 30;
        this.cooldownScope = CooldownScope.USER;
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL};
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "feedback", "The feedback you want to leave", true)
                .setMinLength(10)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        var feedback = event.optString("feedback", "");
        TextChannel feedbackChannel = event.getJDA().getTextChannelById("745164378659225651");
        if (feedbackChannel == null) {
            event.replyEmbeds(ResponseHelper.generateFailureEmbed("Error Sending Feedback!", "Could not find feedback channel."))
                .setEphemeral(true).queue();
            return;
        }

        feedbackChannel.sendMessageEmbeds(generateFeedbackEmbed(feedback, event.getUser())).queue(
            message -> event.reply("I have successfully sent the feedback! Feel free to see it on the help server with `/invite`")
                .setEphemeral(true)
                .queue()
        );
    }

    /**
     * Generates the feedback embed
     *
     * @param feedback The feedback to send
     * @param author The author of the feedback
     * @return The feedback embed
     */
    private MessageEmbed generateFeedbackEmbed(String feedback, User author) {
        return new EmbedBuilder()
            .setTitle("New Feedback!")
            .setColor(0x6166A8)
            .setDescription(feedback)
            .setTimestamp(Instant.now())
            .setAuthor(author.getAsTag(), null, author.getAvatarUrl())
            .setFooter("User ID: " + author.getId()).build();
    }
}
