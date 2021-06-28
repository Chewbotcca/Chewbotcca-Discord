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
package pw.chew.chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import pw.chew.chewbotcca.util.ResponseHelper;

import java.awt.Color;
import java.util.Collections;
import java.util.Random;

// %^8ball command
public class EightBallCommand extends SlashCommand {
    // The good responses
    final String[] goodResponses = new String[]{
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
    final String[] neutralResponses = new String[]{
            "Better not tell you now!",
            "Ask again later.",
            "Cannot predict now",
            "Cool down enabled! Please try again.",
            "Concentrate and ask again.",
            "Rhetorical questions can be answered in solo",
            "Maybe..."
    };
    // The bad responses
    final String[] badResponses = new String[]{
            "You're kidding, right?",
            "Don't count on it.",
            "In your dreams",
            "My reply is no",
            "Outlook not so good",
            "My undisclosed sources say NO",
            "One would be wise to think not",
            "Very doubtful"
    };
    // A Random object
    final Random rand = new Random();

    public EightBallCommand() {
        this.name = "8ball";
        this.help = "Ask the magic 8ball a question!";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "question", "The question to ask the eight ball").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.replyEmbeds(generateEmbed(ResponseHelper.guaranteeStringOption(event, "question", ""))).queue();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Get the question, doesn't matter what they said but we'll send it back to them
        String question = commandEvent.getArgs();
        commandEvent.reply(generateEmbed(question));
    }

    private MessageEmbed generateEmbed(String question) {
        // Pick a number between 0 and 2 inclusive
        int response = rand.nextInt(3);
        EmbedBuilder e = new EmbedBuilder();
        e.setTitle(":question: Question");
        e.setDescription(question);
        String answer = null;
        // Set the answer based on the random response
        switch (response) {
            case 0 -> {
                answer = getRandom(goodResponses);
                e.setColor(Color.decode("#00FF00"));
            }
            case 1 -> {
                answer = getRandom(neutralResponses);
                e.setColor(Color.decode("#FFFF00"));
            }
            case 2 -> {
                answer = getRandom(badResponses);
                e.setColor(Color.decode("#FF0000"));
            }
        }
        // Finish and send embed
        e.addField(":8ball: 8ball says", answer, false);
        return e.build();
    }

    // Method to get a random string from an array
    public static String getRandom(String[] array) {
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }
}
