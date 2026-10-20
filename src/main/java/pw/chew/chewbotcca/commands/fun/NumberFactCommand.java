/*
 * Copyright (C) 2026 Chewbotcca
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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import pw.chew.chewbotcca.util.CommandContext;
import pw.chew.chewbotcca.util.RestClient;

import java.util.Arrays;

/**
 * <h2><code>/numberfact</code> Command</h2>
 *
 * <a href="https://help.chew.pro/bots/discord/chewbotcca/commands/numberfact">Docs</a>
 */
public class NumberFactCommand extends SlashCommand {

    public NumberFactCommand() {
        this.name = "numberfact";
        this.help = "Find some cool and interesting facts about a number";
        this.contexts = CommandContext.GLOBAL;
        this.options = Arrays.asList(
            new OptionData(OptionType.INTEGER, "number", "The number to get facts about").setRequired(true),
            new OptionData(OptionType.STRING, "type", "The type of fact to return")
                .addChoice("Trivia (default)", "trivia")
                .addChoice("Math", "math")
                .addChoice("Date", "date")
                .addChoice("Year", "year")
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.replyEmbeds(gatherFact(
            event.optString("number", "0"),
            event.optString("type", "trivia")
        )).queue();
    }

    private MessageEmbed gatherFact(String number, String type) {
        // Generate the url based off of the type
        String url = switch (type) {
            default -> "http://number-trivia.com/" + number + "?notfound=";
            case "math", "year", "date" -> "http://number-trivia.com/" + number + "/" + type + "?notfound=";
        };

        EmbedBuilder embed = new EmbedBuilder();

        // Attempt to get a fact
        String facto = RestClient.get(url + "floor").asString();
        // If the number is too low, get a higher number
        if(facto.equals("-Infinity is negative infinity."))
            facto = RestClient.get(url + "ceil").asString();

        // Set and return the fact embed
        embed.setTitle("Did you know?");
        embed.setColor(0x85bae7);
        embed.setDescription(facto);
        embed.setAuthor("Number Facts!");
        if (!facto.split(" ")[0].equals(number) && !type.equals("date"))
            embed.setFooter("Your number didn't have a fact, so a number was approximated for you.");

        return embed.build();
    }
}
