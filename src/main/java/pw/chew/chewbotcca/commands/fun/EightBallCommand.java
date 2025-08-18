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
package pw.chew.chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import pw.chew.chewbotcca.util.MiscUtil;

import java.awt.Color;
import java.util.Collections;

/**
 * <h2><code>/8ball</code> Command</h2>
 *
 * <a href="https://help.chew.pro/bots/discord/chewbotcca/commands/8ball">Docs</a>
 */
public class EightBallCommand extends SlashCommand {
    // The good responses
    final String[] goodResponses = {
        "As I see it, yes",
        "It is certain",
        "It is decidedly so",
        "Most likely",
        "Outlook good",
        "Signs point to yes",
        "One would be wise to think so",
        "Naturally",
        "Without a doubt",
        "Yes",
        "You may rely on it",
        "You can count on it"
    };
    // The neutral responses
    final String[] neutralResponses = {
        "Better not tell you now!",
        "Ask again later.",
        "Cannot predict now",
        "Cool down enabled! Please try again.",
        "Concentrate and ask again.",
        "Rhetorical questions can be answered in solo",
        "Maybe..."
    };
    // The bad responses
    final String[] badResponses = {
        "You're kidding, right?",
        "Don't count on it.",
        "In your dreams",
        "My reply is no",
        "Outlook not so good",
        "My undisclosed sources say NO",
        "One would be wise to think not",
        "Very doubtful"
    };

    public EightBallCommand() {
        this.name = "8ball";
        this.help = "Ask the magic 8ball a question!";
        this.contexts = new InteractionContextType[]{InteractionContextType.BOT_DM, InteractionContextType.GUILD, InteractionContextType.PRIVATE_CHANNEL};
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "question", "The question to ask the eight ball").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.replyComponents(buildResponse(event.optString("question", ""))).useComponentsV2().queue();
    }

    private Container buildResponse(String question) {
        // Pick a number between 0 and 2 inclusive
        int response = MiscUtil.getRandom(0, 1, 2);
        String answer;
        Color color;
        // Set the answer based on the random response
        switch (response) {
            case 0 -> {
                answer = MiscUtil.getRandom(goodResponses);
                color = Color.RED;
            }
            case 1 -> {
                answer = MiscUtil.getRandom(neutralResponses);
                color = Color.YELLOW;
            }
            default -> {
                answer = MiscUtil.getRandom(badResponses);
                color = Color.GREEN;
            }
        }

        // Finish and send container
        return Container.of(
            TextDisplay.of("### :question: Question"),
            TextDisplay.of(question),
            Separator.createDivider(Separator.Spacing.SMALL),
            TextDisplay.of("### :8ball: 8ball says..."),
            TextDisplay.of(answer)
        ).withAccentColor(color);
    }
}
