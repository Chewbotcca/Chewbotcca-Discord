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
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

// %^cat command
public class CatCommand extends Command {

    public CatCommand() {
        this.name = "cat";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Try to gather a cat from the Cat API
        try {
            String showcat = new JSONObject(RestClient.get("http://aws.random.cat/meow")).getString("file");

            commandEvent.reply(new EmbedBuilder()
                .setTitle("Adorable.", showcat)
                .setImage(showcat)
                .build()
            );
        } catch (JSONException e) {
            // If cat collecting failed :(
            commandEvent.reply(new EmbedBuilder()
                .setTitle("Sad mews!")
                .setDescription("The Cat API is not working. :( Try again later? :3")
                .build()
            );
        }
    }
}