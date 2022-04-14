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
package pw.chew.chewbotcca.menus.message;

import com.jagrosh.jdautilities.command.MessageContextMenu;
import com.jagrosh.jdautilities.command.MessageContextMenuEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import pw.chew.chewbotcca.listeners.ReactListener;

public class UnfurlMessageContextMenu extends MessageContextMenu {
    public UnfurlMessageContextMenu() {
        this.name = "Unfurl Link";
    }

    @Override
    protected void execute(MessageContextMenuEvent event) {
        Message message = event.getTarget();

        MessageEmbed unfurl = ReactListener.unfurlMessage(message);

        if (unfurl != null) {
            event.replyEmbeds(unfurl).setEphemeral(true).queue();
        } else {
            event.reply("""
                Could not find a link to unfurl.
                
                Supported sites: YouTube (videos), GitHub (issues/PRs), Mojira/Spigot Jira (bugs), and Memerator (memes/users).
                
                If you think this is a bug, or you think it should be unfurled, please [report it](https://github.com/Chewbotcca/Discord/issues).
                """).setEphemeral(true).queue();
        }
    }
}
