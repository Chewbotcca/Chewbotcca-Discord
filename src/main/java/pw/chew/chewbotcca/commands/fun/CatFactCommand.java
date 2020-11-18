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
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

// %^catfact command
public class CatFactCommand extends Command {

    public CatFactCommand() {
        this.name = "catfact";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Get a fact and respond with it
        JSONObject data = new JSONObject(RestClient.get("https://catfact.ninja/fact"));
        if (data.has("error")) {
            commandEvent.reply("Could not get cat fact :cry: Error: " + data.getString("error"));
            return;
        }
        String fact = data.getString("fact");
        commandEvent.reply(fact);
    }
}