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
import pw.chew.chewbotcca.objects.ServerSettings;
import pw.chew.chewbotcca.util.PropertiesManager;

import java.util.ArrayList;
import java.util.List;

public class PrefixCommand extends Command {

    public PrefixCommand() {
        this.name = "prefix";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        List<String> prefixes = new ArrayList<>();
        prefixes.add("Global prefix: `" + PropertiesManager.getPrefix() + "` or mention me.");

        if (event.getEvent().isFromGuild()) {
            ServerSettings ss = ServerSettings.getServer(event.getGuild().getId());
            if (ss.getPrefix() != null) {
                prefixes.add("Custom Server Prefix: `" + ss.getPrefix() + "`");
            }
            prefixes.add("Invoked prefix: `" + event.getPrefix() + "`");
        } else {
            prefixes.add("In DMs, no prefix is needed.");
        }

        event.reply(String.join("\n", prefixes));
    }
}
