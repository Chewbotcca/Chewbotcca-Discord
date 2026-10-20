/*
 * Copyright (C) 2026 Chewbotcca
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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.CommandContext;
import pw.chew.chewbotcca.util.ResponseHelper;
import pw.chew.chewbotcca.util.RestClient;

/**
 * <h2><code>/cat</code> Command</h2>
 *
 * <a href="https://help.chew.pro/bots/discord/chewbotcca/commands/cat">Docs</a>
 */
public class CatCommand extends SlashCommand {
    public CatCommand() {
        this.name = "cat";
        this.help = "Get a very cute picture of a cat!";
        this.contexts = CommandContext.GLOBAL;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.replyEmbeds(generateCatEmbed()).queue();
    }

    private MessageEmbed generateCatEmbed() {
        // Try to gather a cat from the Cat API
        try {
            JSONObject cat = RestClient.get("https://random.cat/api/random").asJSONObject();

            return (new EmbedBuilder()
                .setTitle("Adorable.", cat.getString("page"))
                .setImage(cat.getString("url"))
                .setFooter("ID: " + cat.getString("id"))
                .build()
            );
        } catch (JSONException e) {
            // If cat collecting failed :(
            return (ResponseHelper.generateFailureEmbed("Sad news!", "The Cat API is not working. :( Try again later? :3"));
        }
    }
}
