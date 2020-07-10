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

// %^acronym command
public class AcronymCommand extends Command {

    public AcronymCommand() {
        this.name = "acronym";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        // Get acronym and send if the acronym is valid
        try {
            String phrase = new ChewAPI().generateAcronym(event.getArgs());
            event.reply("Acronym for " + event.getArgs() + " is " + phrase);
        } catch (IllegalArgumentException e) {
            event.reply("Args must only contain letters!");
        }
    }
}
