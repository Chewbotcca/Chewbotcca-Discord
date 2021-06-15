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
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import pw.chew.chewbotcca.util.ResponseHelper;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

// %^roll command
public class RollCommand extends SlashCommand {
    public RollCommand() {
        this.name = "roll";
        this.guildOnly = false;
        this.help = "Rolls an optionally specified amount of dice";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};

        this.options = Arrays.asList(
            new OptionData(OptionType.INTEGER, "dice", "The amount of dice to throw. Default 1"),
            new OptionData(OptionType.INTEGER, "sides", "The amount of sides on a die. Default 6.")
        );
    }

    @Override
    protected void execute(SlashCommandEvent slashCommandEvent) {
        int dice = 1;
        int sides = 6;
        for (OptionMapping mapping : slashCommandEvent.getOptions()) {
            switch (mapping.getName()) {
                case "dice":
                    dice = (int) mapping.getAsLong();
                case "sides":
                    sides = (int) mapping.getAsLong();
            }
        }

        try {
            slashCommandEvent.replyEmbeds(generateEmbed(dice, sides)).queue();
        } catch (IllegalArgumentException e) {
            slashCommandEvent.replyEmbeds(ResponseHelper.generateFailureEmbed(null, e.getMessage()))
                .setEphemeral(true)
                .queue();
        }
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.getChannel().sendTyping().queue();
        // Get the args, if there's no args, just use a default 1d6
        String args = commandEvent.getArgs();
        if(args.isBlank()) {
            args = "1d6";
        }

        // Parse the args into dice and sides
        String[] types = args.split("d");
        int dice;
        int sides;
        try {
            dice = Integer.parseInt(types[0].trim());
            if (types.length < 2) {
                sides = 6;
            } else {
                sides = Integer.parseInt(types[1].trim());
            }
        } catch (NumberFormatException e) {
            commandEvent.reply("Your input is too big! Try again, but with lower numbers.");
            return;
        }

        try {
            commandEvent.reply(generateEmbed(dice, sides));
        } catch (IllegalArgumentException e) {
            commandEvent.reply(e.getMessage());
        }
    }

    private MessageEmbed generateEmbed(int dice, int sides) {
        // If the args are invalid, let them know
        if(dice < 1) {
            throw new IllegalArgumentException("You must roll at least 1 die.");
        }
        if(sides < 1) {
            throw new IllegalArgumentException("Sides cannot be less than 1!");
        }
        // Do the math to calculate the dice roll
        long total = 0;
        for(int i = 0; i < dice; i++) {
            total += ThreadLocalRandom.current().nextInt(0, sides) + 1;
        }

        // Take the data, make an embed, and send it off
        return (new EmbedBuilder()
                .setTitle("Dice Roll \uD83C\uDFB2")
                .addField("Dice", NumberFormat.getNumberInstance(Locale.US).format(dice), true)
                .addField("Sides", NumberFormat.getNumberInstance(Locale.US).format(sides), true)
                .addField("Total", NumberFormat.getNumberInstance(Locale.US).format(total), false)
                .build()
        );
    }
}

