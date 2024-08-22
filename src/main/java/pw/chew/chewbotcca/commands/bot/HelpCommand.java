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
package pw.chew.chewbotcca.commands.bot;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import pw.chew.chewbotcca.util.CommandMentionHelper;

/**
 * <h2><code>/help</code> Command</h2>
 *
 * <a href="https://help.chew.pro/bots/discord/chewbotcca/commands/help">Docs</a>
 */
public class HelpCommand extends SlashCommand {
    public HelpCommand() {
        this.name = "help";
        this.help = "Get Help with the bot";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.replyEmbeds(new EmbedBuilder()
            .setTitle("Welcome to the Chewbotcca Discord Bot")
            .setColor(0xd084)
            .setDescription("""
                    Chewbotcca is a multi-purpose, semi-functional, almost always online, Discord bot!

                    This bot is powered by [SkySilk Cloud Services](https://www.skysilk.com/ref/4PRQpuQraD)!""")
            .addField("Commands", "You can find all my commands [here](https:/help.chew.pro/bots/discord/chewbotcca/commands)", true)
            .addField("Invite me!", "You can invite me to your server with [this link](https://discord.com/oauth2/authorize?client_id=604362556668248095&scope=bot&permissions=0).", true)
            .addField("Help Server", "Click [me](https://discord.gg/UjxQ3Bh) to join the help server.", true)
            .addField("Privacy Policy", "[View Privacy Policy](https://chew.pw/chewbotcca/discord/privacy)", true)
            .addField("More Bot Stats", "Run %s to see more stats!".formatted(CommandMentionHelper.mention("stats")), true)
            .build()).setEphemeral(true).queue();
    }
}
