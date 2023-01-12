/*
 * Copyright (C) 2023 Chewbotcca
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

package pw.chew.chewbotcca.util;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.ICommandReference;
import org.jetbrains.annotations.NotNull;
import pw.chew.chewbotcca.objects.Memory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CommandMentionHelper {
    private static List<Command> commands = new ArrayList<>();

    /**
     * Mentions a command based on the name
     *
     * @param command the command to mention
     * @return the command mention, or null if not found
     */
    @Nullable
    public static String mention(@NotNull String command) {
        // If empty command list, retrieve it
        if (commands.isEmpty()) {
            String forcedServerId = PropertiesManager.forceGuildId();
            if (forcedServerId == null) {
                commands = Memory.getJda().retrieveCommands().complete();
            } else {
                commands = Memory.getJda().getGuildById(forcedServerId).retrieveCommands().complete();
            }
        }

        // Iterate through command
        for (Command c : commands) {
            List<ICommandReference> references = new ArrayList<>();
            if (c.getSubcommands().isEmpty()) {
                references.add(c);
            } else {
                references.addAll(c.getSubcommands());
            }

            for (ICommandReference reference : references) {
                if (reference.getFullCommandName().equalsIgnoreCase(command)) {
                    return reference.getAsMention();
                }
            }
        }

        return null;
    }
}
