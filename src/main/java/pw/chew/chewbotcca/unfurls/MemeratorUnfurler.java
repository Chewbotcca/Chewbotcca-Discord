/*
 * Copyright (C) 2023 Chewbotcca
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

package pw.chew.chewbotcca.unfurls;

import me.memerator.api.client.entities.Meme;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.Nullable;
import pw.chew.chewbotcca.commands.services.MemeratorCommand;

import static pw.chew.chewbotcca.commands.services.MemeratorCommand.MemeratorMemeSubCommand.generateMemeEmbed;
import static pw.chew.chewbotcca.commands.services.MemeratorCommand.MemeratorUserSubCommand.generateUserEmbed;

public class MemeratorUnfurler implements GenericUnfurler {
    @Override
    public boolean checkLink(String link) {
        return link.contains("memerator.me") && (link.contains("/p") || link.contains("/m"));
    }

    @Override
    public @Nullable MessageEmbed unfurl(String link) {
        if (link.contains("/p")) {
            return handleUser(link);
        } else if (link.contains("me/m")) {
            return handleMeme(link);
        }
        return null;
    }

    public MessageEmbed handleMeme(String link) {
        String id = link.split("/")[link.split("/").length - 1];
        if (!id.toLowerCase().matches("([a-f]|\\d){6,7}")) {
            return null;
        }

        // Get the meme
        Meme meme = MemeratorCommand.MemeratorMemeSubCommand.getMeme(id, true);

        // If it's null, return null
        if (meme == null)
            return null;

        return generateMemeEmbed(meme).build();
    }

    public MessageEmbed handleUser(String link) {
        String name = link.split("/")[link.split("/").length - 1];

        // Get the user
        var user = MemeratorCommand.getAPI().retrieveUser(name).complete();

        // If the user doesn't exist, return null
        if (user == null)
            return null;

        // Return the user embed
        return generateUserEmbed(user).build();
    }
}
