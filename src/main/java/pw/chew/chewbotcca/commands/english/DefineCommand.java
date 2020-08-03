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
package pw.chew.chewbotcca.commands.english;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONArray;
import org.json.JSONException;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.RestClient;

// %^define command
public class DefineCommand extends Command {

    public DefineCommand() {
        this.name = "define";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Get the word from the command
        String word = commandEvent.getArgs();
        // Attempt to grab the word, if it doesn't exist let them know
        JSONArray grabbedword;
        try {
            grabbedword = new JSONArray(RestClient.get("http://api.wordnik.com/v4/word.json/" + word + "/definitions?limit=1&includeRelated=true&useCanonical=false&includeTags=false&api_key=" + PropertiesManager.getWordnikToken()));
        } catch(JSONException e) {
            commandEvent.reply("Word not found! Check your local spell-checker!");
            return;
        }

        if(!grabbedword.getJSONObject(0).has("text")) {
            commandEvent.reply("Word has no definition! Why does it exist then!");
            return;
        }

        // Build the definition embed
        EmbedBuilder e = new EmbedBuilder()
                .setTitle("Definition for " + word)
                .setColor(0xd084)
                .setDescription(grabbedword.getJSONObject(0).getString("text"))
                .setAuthor("Dictionary", null, "https://icons.iconarchive.com/icons/johanchalibert/mac-osx-yosemite/1024/dictionary-icon.png");

        // Only put part of speech if there is one
        if(grabbedword.getJSONObject(0).has("partOfSpeech")) {
            e.addField("Part of Speech", grabbedword.getJSONObject(0).getString("partOfSpeech"), true);
        }

        // Send it off!
        commandEvent.reply(e.build());
    }
}
