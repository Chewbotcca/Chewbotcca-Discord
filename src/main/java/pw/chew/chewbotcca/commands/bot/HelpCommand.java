/*
 * Copyright (C) 2021 Chewbotcca
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
package pw.chew.chewbotcca.commands.bot;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

// %^help command
public class HelpCommand extends SlashCommand {
    public HelpCommand() {
        this.name = "help";
        this.help = "Get Help with the bot";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.replyEmbeds(generateHelpEmbed("/")).queue();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Reply with embed
        String notice = null;
        if (!commandEvent.getArgs().isEmpty()) {
            notice = "Psst, to view more info about a command, use `" + commandEvent.getPrefix() + "info command`!";
        }
        MessageEmbed embed = generateHelpEmbed(commandEvent.getPrefix());
        if (notice == null)
            commandEvent.getChannel().sendMessageEmbeds(embed).queue();
        else
            commandEvent.getChannel().sendMessage(notice).setEmbeds(embed).queue();
    }

    private MessageEmbed generateHelpEmbed(String prefix) {
        return new EmbedBuilder()
            .setTitle("Welcome to the Chewbotcca Discord Bot")
            .setColor(0xd084)
            .setDescription("""
                    Chewbotcca is a multi-purpose, semi-functional, almost always online, Discord bot!

                    This bot is powered by [SkySilk Cloud Services](https://www.skysilk.com/ref/4PRQpuQraD)!""")
            .addField("Commands", "You can find all my commands [here](https://chew.pw/chewbotcca/discord/commands)", true)
            .addField("Invite me!", "You can invite me to your server with [this link](https://discord.com/oauth2/authorize?client_id=604362556668248095&scope=bot&permissions=0).", true)
            .addField("Help Server", "Click [me](https://discord.gg/UjxQ3Bh) to join the help server.", true)
            .addField("More Bot Stats", "Run `" + prefix + "stats` to see more stats!", true)
            .build();
    }
}
