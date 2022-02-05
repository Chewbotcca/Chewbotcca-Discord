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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

// %^dog command
public class DogCommand extends SlashCommand {

    public DogCommand() {
        this.name = "dog";
        this.help = "Gets a random dog picture! Bark, woof, purr?";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Get a dog and bark it, i mean send it, am not furry i swear
        String dog = new JSONObject(RestClient.get("https://random.dog/woof.json")).getString("url");

        event.replyEmbeds(new EmbedBuilder()
            .setTitle("Adorable.", dog)
            .setImage(dog)
            .build()
        ).queue();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Get a dog and woof it, i mean send it, am not furry i swear
        String dog = new JSONObject(RestClient.get("https://random.dog/woof.json")).getString("url");

        commandEvent.reply(new EmbedBuilder()
            .setTitle("Adorable.", dog)
            .setImage(dog)
            .build()
        );
    }
}
