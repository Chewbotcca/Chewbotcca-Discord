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
import pw.chew.chewbotcca.util.CommandContext;
import pw.chew.chewbotcca.util.RestClient;

/**
 * <h2><code>/trbmb</code> Command</h2>
 *
 * <a href="https://help.chew.pro/bots/discord/chewbotcca/commands/trbmb">Docs</a>
 */
public class TRBMBCommand extends SlashCommand {
    public TRBMBCommand() {
        this.name = "trbmb";
        this.help = "Generates a random 'That really [blanks] my [blank]' phrase";
        this.contexts = CommandContext.GLOBAL;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String phrase = RestClient.get("https://api.chew.pro/trbmb").asJSONArray().getString(0);
        event.reply(phrase).queue();
    }
}
