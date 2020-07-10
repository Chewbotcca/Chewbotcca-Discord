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
package pw.chew.chewbotcca.commands.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import java.util.ArrayList;

// %^info command
public class InfoCommand extends Command {
    public InfoCommand() {
        this.name = "info";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        // Make sure there's an arg
        String command = event.getArgs();
        if(command.length() == 0) {
            event.reply("Please specify a command to find info for!");
            return;
        }
        // Get the command from the chew api
        JSONObject data = new JSONObject(RestClient.get("https://chew.pw/chewbotcca/discord/api/command/" + command));
        // If there's an error
        if(data.has("error")) {
            JSONArray didYouMean = data.getJSONArray("didYouMean");
            ArrayList<String> predictions = new ArrayList<>();
            for(int i = 0; i < didYouMean.length(); i++) {
                predictions.add(didYouMean.getString(i));
            }
            if(predictions.size() > 0) {
                event.reply("Invalid command! See <https://chew.pw/chewbotcca/discord/commands> for a list of commands. Did you mean? " + String.join(", ", predictions));
            } else {
                event.reply("Invalid command! See <https://chew.pw/chewbotcca/discord/commands> for a list of commands.");
            }
            return;
        }

        // Gather the data and make an embed with it
        EmbedBuilder e = new EmbedBuilder()
                .setTitle("**Info For**: `%^" + data.getString("command") + "`")
                .setDescription(data.getString("description"));
        if(!data.isNull("args")) {
            e.addField("Arguments", data.getString("args"), true);
        } else {
            e.addField("Arguments", "No arguments", true);
        }
        if(!data.isNull("aliases")) {
            e.addField("Aliases", data.getString("aliases"), true);
        } else {
            e.addField("Aliases", "No aliases", true);
        }

        event.reply(e.build());
    }
}

