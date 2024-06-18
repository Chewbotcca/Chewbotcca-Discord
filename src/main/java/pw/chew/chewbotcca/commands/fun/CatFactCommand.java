/*
 * Copyright (C) 2024 Chewbotcca
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
import net.dv8tion.jda.api.interactions.InteractionContextType;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

// %^catfact command
public class CatFactCommand extends SlashCommand {

    public CatFactCommand() {
        this.name = "catfact";
        this.help = "Find a fun fact about our furry feline friends!";
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply(getFact()).queue();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.reply(getFact());
    }

    private String getFact() {
        // Get a fact and respond with it
        JSONObject data = RestClient.get("https://catfact.ninja/fact").asJSONObject();
        if (data.has("error")) {
            return "Could not get cat fact :cry: Error: " + data.getString("error");
        }
        return data.getString("fact");
    }
}
