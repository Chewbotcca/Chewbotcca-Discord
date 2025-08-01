/*
 * Copyright (C) 2025 Chewbotcca
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
package pw.chew.chewbotcca.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pw.chew.chewbotcca.objects.ServerSettings;

public class MessageHandler extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        if (event.getChannel().getId().equals("745164378659225651") && event.getAuthor().getDiscriminator().equals("0000") && !event.getMessage().getEmbeds().isEmpty()) {
            String title = event.getMessage().getEmbeds().getFirst().getTitle();
            if (title != null && title.contains("[Discord] Compile success on main")) {
                event.getChannel().sendMessage("Updating...").queue(message -> System.exit(0));
            }
        }

        // Get server and ensure it's cached
        ServerSettings.getServer(event.getGuild().getId());
    }
}
