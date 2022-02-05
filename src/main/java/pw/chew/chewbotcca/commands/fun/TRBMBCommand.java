/*
 * Copyright (C) 2022 Chewbotcca
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
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import pw.chew.chewbotcca.objects.Memory;

// %^trbmb command
public class TRBMBCommand extends SlashCommand {

    public TRBMBCommand() {
        this.name = "trbmb";
        this.help = "Generates a random TRBMB phrase";
        this.guildOnly = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply(Memory.getChewAPI().getTRBMBPhrase()).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        // Get and send TRBMB Phrase
        event.reply(Memory.getChewAPI().getTRBMBPhrase());
    }
}
