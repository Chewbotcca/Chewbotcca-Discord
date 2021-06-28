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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import pro.chew.api.objects.SpigotDrama;
import pw.chew.chewbotcca.objects.Memory;

// %^spigotdrama command
public class SpigotDramaCommand extends SlashCommand {
    public SpigotDramaCommand() {
        this.name = "spigotdrama";
        this.guildOnly = false;
        this.help = "Generates some random Spigot drama";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    protected void execute(SlashCommandEvent slashCommandEvent) {
        slashCommandEvent.replyEmbeds(generateDramaEmbed()).queue();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.reply(generateDramaEmbed());
    }

    private MessageEmbed generateDramaEmbed() {
        // Get a SpigotDrama response
        SpigotDrama response = Memory.getChewAPI().generateSpigotDrama();
        // Make an embed and send it off
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("mdcfe", "https://github.com/mdcfe/spigot-drama-generator", "https://avatars0.githubusercontent.com/u/1917406");
        embed.setTitle("Spigot Drama Generator", "https://drama.essentialsx.net/");
        embed.setDescription(response.getPhrase() + "\n\n" + "[Permalink](" + response.getPermalink() + ")");

        return embed.build();
    }
}
