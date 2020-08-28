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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

// %^role command
public class RoleCommand extends Command {

    public RoleCommand() {
        this.name = "role";
        this.botPermissions = new Permission[]{Permission.MANAGE_ROLES};
        this.userPermissions = new Permission[]{Permission.MANAGE_ROLES};
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split(" ");
        if(args.length < 1) {
            event.reply("You appear to be missing an argument. Please specify: `create`, `delete`, `assign` (or `add`), `remove`");
            return;
        }

        String type = args[0].toLowerCase();
        String arg = event.getArgs().replace(type + " ", "");
        event.reply(switch (type) {
            case ("create") -> createRole(event, arg);
            case ("delete") -> deleteRole(event, arg);
            case ("add"), ("assign") -> assignRole(event, arg);
            case ("remove") -> removeRole(event, arg);
            default -> "Invalid type, must be one of `create`, `delete`, `assign` (or `add`), `remove`";
        });
    }

    public String createRole(CommandEvent event, String rolename) {
        Guild server = event.getGuild();
        server.createRole().setName(rolename).complete();
        return "I have successfully created the role `" + rolename + "`!";
    }

    public String deleteRole(CommandEvent event, String rolename) {
        Guild server = event.getGuild();
        Role highest = event.getMember().getRoles().get(0);
        List<Role> roles = server.getRolesByName(rolename, true);
        if(roles.size() < 1) {
            return "Unable to find specified role!";
        }
        Role target = roles.get(0);
        if(target.getPosition() >= highest.getPosition()) {
            return "I can't delete this role because it is higher or equal to your highest role!";
        }
        target.delete().queue();
        return "I have successfully deleted the role `" + rolename + "`!";
    }

    public String assignRole(CommandEvent event, String input) {
        String[] args = input.split(" ");
        if(args.length < 2) {
            return "Missing one or more arguments for this command. You need: user mention and role name.";
        }

        String mention = args[0];
        String rolename = input.replace(args[0] + " ", "");

        String id = mention.replace("<@!", "").replace(">", "");
        Member member = event.getGuild().getMemberById(id);

        if(member == null) {
            return "Unable to find specified member!";
        }

        Role highest = event.getMember().getRoles().get(0);
        List<Role> roles = event.getGuild().getRolesByName(rolename, true);
        if(roles.size() < 1) {
            return "Unable to find specified role!";
        }
        Role target = roles.get(0);
        if(target.getPosition() >= highest.getPosition()) {
            return "I can't assign this role because it is higher or equal to your highest role!";
        }

        event.getGuild().addRoleToMember(member, target).queue();
        return "I have successfully given " + member.getEffectiveName() + " the role `" + rolename + "`!";
    }

    public String removeRole(CommandEvent event, String input) {
        String[] args = input.split(" ");
        if(args.length < 2) {
            return "Missing one or more arguments for this command. You need: user mention and role name.";
        }

        String mention = args[0];
        String rolename = input.replace(args[0] + " ", "");

        String id = mention.replace("<@!", "").replace(">", "");
        Member member = event.getGuild().getMemberById(id);

        if(member == null) {
            return "Unable to find specified member!";
        }

        Role highest = event.getMember().getRoles().get(0);
        List<Role> roles = event.getGuild().getRolesByName(rolename, true);
        if(roles.size() < 1) {
            return "Unable to find specified role!";
        }
        Role target = roles.get(0);
        if(target.getPosition() >= highest.getPosition()) {
            return "I can't assign this role because it is higher or equal to your highest role!";
        }

        event.getGuild().removeRoleFromMember(member, target).queue();
        return "I have successfully removed `" + rolename + "` from " + member.getEffectiveName() + "!";
    }
}
