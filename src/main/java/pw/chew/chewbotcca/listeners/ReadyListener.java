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

package pw.chew.chewbotcca.listeners;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import pw.chew.chewbotcca.objects.Memory;

import java.util.ArrayList;
import java.util.List;

public class ReadyListener extends ListenerAdapter {
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        // Get all commands
        List<CommandData> data = new ArrayList<>();
        CommandClient client = Memory.getClient();

        for (SlashCommand command : client.getSlashCommands()) {
            data.add(command.buildCommandData());
        }

        if (client.isManualUpsert()) {
            if (client.forcedGuildId() != null) {
                Guild server = event.getJDA().getGuildById(client.forcedGuildId());
                if (server == null) {
                    LoggerFactory.getLogger(this.getClass()).error("Server used for slash command testing is null!");
                    return;
                }
                server.updateCommands().addCommands(data).queue(commands -> LoggerFactory.getLogger(this.getClass()).debug("Updated slash commands!"));
            } else {
                event.getJDA().updateCommands().addCommands(data).queue(commands -> LoggerFactory.getLogger(this.getClass()).debug("Updated slash commands!"));
            }
        }
    }
}
