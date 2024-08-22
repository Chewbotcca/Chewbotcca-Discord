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

/**
 * <h2><code>/invite</code> Command</h2>
 *
 * <a href="https://help.chew.pro/bots/discord/chewbotcca/commands/invite">Docs</a>
 */
public class InviteCommand extends SlashCommand {
    public InviteCommand() {
        this.name = "invite";
        this.help = "Generate a link to invite me to your serve!";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.replyEmbeds(new EmbedBuilder()
            .setTitle("Invite me!")
            .setDescription("""
                    [Click me to invite me to your server (recommended)](https://discord.com/api/oauth2/authorize?client_id=604362556668248095&permissions=939879492&scope=bot%20applications.commands)!
                    [Click me to invite me to your server (admin)](https://discord.com/api/oauth2/authorize?client_id=604362556668248095&permissions=8&scope=bot%20applications.commands)!
                    
                    [Need help? Click me to join my help server](https://discord.gg/UjxQ3Bh)!
                    
                    [Sponsored: Click me to get a VPS from SkySilk Cloud Services](https://www.skysilk.com/ref/4PRQpuQraD)!""").build()).setEphemeral(true).queue();
    }
}
