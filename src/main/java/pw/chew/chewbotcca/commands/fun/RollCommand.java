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

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

// %^roll command
public class RollCommand extends Command {
    public RollCommand() {
        this.name = "roll";
        this.guildOnly = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
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
            // Max_value + 1 overflows
            if (sides == Integer.MAX_VALUE) {
                sides--;
            }
        } catch (NumberFormatException e) {
            commandEvent.reply("Your input is too big! Try again, but with lower numbers.");
            return;
        }
        // If the args are invalid, let them know
        if(dice < 1) {
            commandEvent.reply("You must roll at least 1 die.");
            return;
        }
        if(sides < 1) {
            commandEvent.reply("Sides cannot be less than 1!");
            return;
        }
        // Do the math to calculate the dice roll
        long total = 0;
        for(int i = 0; i < dice; i++) {
            total += ThreadLocalRandom.current().nextInt(1, sides + 1);
        }

        // Take the data, make an embed, and send it off
        commandEvent.reply(new EmbedBuilder()
                .setTitle("Dice Roll \uD83C\uDFB2")
                .addField("Dice", NumberFormat.getNumberInstance(Locale.US).format(dice), true)
                .addField("Sides", NumberFormat.getNumberInstance(Locale.US).format(sides), true)
                .addField("Total", NumberFormat.getNumberInstance(Locale.US).format(total), false)
                .build()
        );
    }
}

