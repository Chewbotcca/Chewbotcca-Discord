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
import pro.chew.api.ChewAPI;
import pro.chew.api.objects.SpigotDrama;

// %^spigotdrama command
public class SpigotDramaCommand extends Command {
    final ChewAPI chew;

    public SpigotDramaCommand(ChewAPI chew) {
        this.name = "spigotdrama";
        this.guildOnly = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.chew = chew;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Get a SpigotDrama response
        SpigotDrama response = chew.generateSpigotDrama();
        // Make an embed and send it off
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("md678685", "https://github.com/md678685/spigot-drama-generator", "https://avatars0.githubusercontent.com/u/1917406");
        embed.setTitle("Spigot Drama Generator", "https://drama.essentialsx.net/");
        embed.setDescription(response.getPhrase() + "\n\n" + "[Permalink](" + response.getPermalink() + ")");

        commandEvent.reply(embed.build());
    }
}
