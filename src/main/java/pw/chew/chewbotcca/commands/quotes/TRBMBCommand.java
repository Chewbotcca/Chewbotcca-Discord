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
package pw.chew.chewbotcca.commands.quotes;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import pro.chew.api.ChewAPI;

// %^trbmb command
public class TRBMBCommand extends Command {
    final ChewAPI chew;

    public TRBMBCommand(ChewAPI chewAPI) {
        this.name = "trbmb";
        this.guildOnly = false;
        this.chew = chewAPI;
    }

    @Override
    protected void execute(CommandEvent event) {
        // Get and send TRBMB Phrase
        event.reply(chew.getTRBMBPhrase());
    }
}
