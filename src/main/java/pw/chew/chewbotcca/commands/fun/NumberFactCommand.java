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
import pw.chew.chewbotcca.util.RestClient;
import pw.chew.jdachewtils.command.OptionHelper;

import java.util.Arrays;

// %^numberfact command
public class NumberFactCommand extends SlashCommand {

    public NumberFactCommand() {
        this.name = "numberfact";
        this.help = "Find some cool and interesting facts about a number";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
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
            OptionHelper.optString(event, "number", "0"),
            OptionHelper.optString(event, "type", "trivia")
        )).queue();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Get the arguments
        String[] args = commandEvent.getArgs().split(" ");
        // If there's no args
        if (args.length == 0) {
            commandEvent.reply("Please specify a number to find a fact for! Also, optionally specify what type of fact, choices: `trivia`, `year`, `date`, `math`.");
            return;
        }
        String number = args[0];
        // If the first arg isn't a number
        try {
            Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            commandEvent.reply("Invalid number input!");
            return;
        }
        // The type if specified
        String type;
        if (args.length > 1) {
            type = args[1].toLowerCase();
        } else {
            type = "trivia";
        }

        commandEvent.reply(gatherFact(number, type));
    }

    private MessageEmbed gatherFact(String number, String type) {
        // Generate the url based off of the type
        String url = switch (type) {
            default -> "http://numbersapi.com/" + number + "?notfound=";
            case "math", "year", "date" -> "http://numbersapi.com/" + number + "/" + type + "?notfound=";
        };

        EmbedBuilder embed = new EmbedBuilder();

        // Attempt to get a fact
        String facto = RestClient.get(url + "floor");
        // If the number is too low, get a higher number
        if(facto.equals("-Infinity is negative infinity."))
            facto = RestClient.get(url + "ceil");

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
