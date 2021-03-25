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

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

// %^invite command
public class InviteCommand extends Command {
    public InviteCommand() {
        this.name = "invite";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.reply(new EmbedBuilder()
                .setTitle("Invite me!")
                .setDescription("""
                    [Click me to invite me to your server (recommended)](https://discord.com/api/oauth2/authorize?client_id=604362556668248095&permissions=939879492&scope=bot%20applications.commands)!
                    [Click me to invite me to your server (admin)](https://discord.com/api/oauth2/authorize?client_id=604362556668248095&permissions=8&scope=bot%20applications.commands)!
                    
                    [Need help? Click me to join my help server](https://discord.gg/UjxQ3Bh)!
                    
                    [Sponsored: Click me to get a VPS from SkySilk Cloud Services](https://www.skysilk.com/ref/4PRQpuQraD)!""").build());
    }
}
