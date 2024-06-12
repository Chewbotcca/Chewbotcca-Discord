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

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import pro.chew.api.objects.SpigotDrama;
import pw.chew.chewbotcca.objects.Memory;

/**
 * <h2><code>/spigotdrama Command</code></h2>
 *
 * <a href="https://help.chew.pro/bots/discord/chewbotcca/commands/spigotdrama">Docs</a>
 */
public class SpigotDramaCommand extends SlashCommand {
    public SpigotDramaCommand() {
        this.name = "spigotdrama";
        this.guildOnly = false;
        this.help = "Generates some random Spigot drama";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Get a SpigotDrama response
        SpigotDrama response = Memory.getChewAPI().generateSpigotDrama();
        // Make an embed and send it off
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("By mdcfe", "https://github.com/mdcfe/spigot-drama-generator", "https://avatars0.githubusercontent.com/u/1917406");
        embed.setTitle("Spigot Drama Generator", "https://drama.mdcfe.dev/");
        embed.setDescription(response.getPhrase());

        event.replyEmbeds(embed.build())
            .addActionRow(Button.link(response.getPermalink(), "Permalink"))
            .queue();
    }
}
