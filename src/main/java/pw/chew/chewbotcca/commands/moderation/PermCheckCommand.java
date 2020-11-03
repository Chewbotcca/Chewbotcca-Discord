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
package pw.chew.chewbotcca.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermCheckCommand extends Command {

    public PermCheckCommand() {
        this.name = "permcheck";
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        Map<Permission, String> needed = new HashMap<>();
        needed.put(Permission.MESSAGE_EMBED_LINKS, "Required for most commands.");
        needed.put(Permission.MESSAGE_ADD_REACTION, "Required for Paginator to work.");
        needed.put(Permission.MESSAGE_EXT_EMOJI, "Makes some commands look nicer.");
        needed.put(Permission.MANAGE_ROLES, "Required for `%^role` command.");
        needed.put(Permission.NICKNAME_MANAGE, "Required for `%^dehoist` command.");
        needed.put(Permission.MANAGE_WEBHOOKS, "Required to view webhook count in `%^cinfo` and for `%^rory follow`.");
        needed.put(Permission.BAN_MEMBERS, "Required for `%^ban` command.");

        List<String> response = new ArrayList<>();
        response.add("Chewbotcca Permission Check - If I'm missing a permission, it will list where/if it's needed.");
        for(Permission perm : needed.keySet()) {
            if (event.getSelfMember().hasPermission(perm)) {
                response.add(":white_check_mark: " + perm.getName());
            } else {
                response.add(":x: " + perm.getName() + " - " + needed.get(perm));
            }
        }

        event.reply(String.join("\n", response));
    }
}
