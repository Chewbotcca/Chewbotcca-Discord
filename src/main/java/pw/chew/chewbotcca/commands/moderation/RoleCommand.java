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
package pw.chew.chewbotcca.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import pw.chew.chewbotcca.util.Mention;

import java.util.ArrayList;
import java.util.List;

// %^role command
public class RoleCommand extends Command {

    public RoleCommand() {
        this.name = "role";
        this.botPermissions = new Permission[]{Permission.MANAGE_ROLES};
        this.userPermissions = new Permission[]{Permission.MANAGE_ROLES};
        this.guildOnly = true;
        this.children = new Command[]{
            new CreateRoleSubCommand(),
            new DeleteRoleSubCommand(),
            new AssignRoleSubCommand(),
            new RemoveRoleSubCommand(),
            new CloneRoleSubCommand()
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        // Ensure user entered an argument
        String[] args = event.getArgs().split(" ");
        if (args.length < 1) {
            event.reply("You appear to be missing an argument. Please specify: `create`, `delete`, `assign` (or `add`), `remove`");
            return;
        }

        event.reply("Invalid type, must be one of `create`, `delete`, `assign` (or `add`), `remove`, or `clone`");
    }

    /**
     * Creates a role with given name
     */
    private static class CreateRoleSubCommand extends Command {
        public CreateRoleSubCommand() {
            this.name = "create";
            this.guildOnly = true;
            this.botPermissions = new Permission[]{Permission.MANAGE_ROLES};
            this.userPermissions = new Permission[]{Permission.MANAGE_ROLES};
        }

        @Override
        protected void execute(CommandEvent event) {
            // Get the server and create the role
            Guild server = event.getGuild();
            server.createRole().setName(event.getArgs()).queue(role -> event.reply("I have successfully created the role `" + event.getArgs() + "`!"));
        }
    }

    /**
     * Deletes a role with a given name
     */
    private class DeleteRoleSubCommand extends Command {
        public DeleteRoleSubCommand() {
            this.name = "delete";
            this.guildOnly = true;
            this.botPermissions = new Permission[]{Permission.MANAGE_ROLES};
            this.userPermissions = new Permission[]{Permission.MANAGE_ROLES};
        }

        @Override
        protected void execute(CommandEvent event) {
            // Get the server
            Guild server = event.getGuild();
            // Attempt to find provided role
            List<Role> roles = server.getRolesByName(event.getArgs(), true);
            if (roles.isEmpty()) {
                event.reply("Unable to find specified role!");
                return;
            }
            // Get the user's highest role and check to make sure hierarchy is maintained
            Role highest = getHighestRole(event.getMember());
            Role bot = getHighestRole(event.getSelfMember());
            Role target = roles.get(0);
            if (target.getPosition() >= highest.getPosition() || target.getPosition() >= bot.getPosition()) {
                event.reply("I can't delete this role because it is higher or equal to your (or my) highest role!");
                return;
            }
            // Delete the specified role
            target.delete().queue(r -> event.reply("I have successfully deleted the role `" + event.getArgs() + "`!"));
        }
    }

    /**
     * Assign a role to a provided member
     */
    private class AssignRoleSubCommand extends Command {
        public AssignRoleSubCommand() {
            this.name = "assign";
            this.aliases = new String[]{"add"};
            this.guildOnly = true;
            this.botPermissions = new Permission[]{Permission.MANAGE_ROLES};
            this.userPermissions = new Permission[]{Permission.MANAGE_ROLES};
        }

        @Override
        protected void execute(CommandEvent event) {
            String input = event.getArgs();
            // Grab input and parse for user and role
            String[] args = input.split(" ");
            if (args.length < 2) {
                event.reply("Missing one or more arguments for this command. You need: user mention and role name.");
                return;
            }

            String mention = args[0];
            String role = input.replace(args[0] + " ", "");
            while (role.startsWith(" ")) {
                role = role.substring(1);
            }

            String rolename = role;

            Object parse = Mention.parseMention(mention, event.getGuild(), event.getJDA());

            if (!(parse instanceof Member member)) {
                event.reply("Unable to find specified member!");
                return;
            }

            // Attempt to find role
            List<Role> roles = event.getGuild().getRolesByName(rolename, true);
            if (roles.isEmpty()) {
                event.reply("Unable to find specified role!");
                return;
            }
            // Get author's highest role for hierarchy checking
            Role highest = getHighestRole(event.getMember());
            Role bot = getHighestRole(event.getSelfMember());
            Role target = roles.get(0);
            if (target.getPosition() >= highest.getPosition() || target.getPosition() >= bot.getPosition()) {
                event.reply("I can't assign this role because it is higher or equal to your (or my) highest role!");
                return;
            }

            // Add role to member and call it a day
            event.getGuild().addRoleToMember(member, target).queue(
                e -> event.reply("I have successfully given " + member.getEffectiveName() + " the role `" + rolename + "`!"),
                f -> event.replyError("I was unable to give that user the role!")
            );
        }
    }

    /**
     * Remove a role from a specific member
     */
    private class RemoveRoleSubCommand extends Command {
        public RemoveRoleSubCommand() {
            this.name = "remove";
            this.guildOnly = true;
            this.botPermissions = new Permission[]{Permission.MANAGE_ROLES};
            this.userPermissions = new Permission[]{Permission.MANAGE_ROLES};
        }

        @Override
        protected void execute(CommandEvent event) {
            String input = event.getArgs();
            // Grab input and parse for user and role
            String[] args = input.split(" ");
            if (args.length < 2) {
                event.reply("Missing one or more arguments for this command. You need: user mention and role name.");
                return;
            }

            String mention = args[0];
            String role = input.replace(args[0] + " ", "");
            while (role.startsWith(" ")) {
                role = role.substring(1);
            }

            String rolename = role;

            Object parse = Mention.parseMention(mention, event.getGuild(), event.getJDA());

            if (!(parse instanceof Member member)) {
                event.reply("Unable to find specified member!");
                return;
            }

            // Attempt to find role
            List<Role> roles = event.getGuild().getRolesByName(rolename, true);
            if (roles.isEmpty()) {
                event.reply("Unable to find specified role!");
                return;
            }
            // Get author's highest role for hierarchy checking
            Role highest = getHighestRole(event.getMember());
            Role bot = getHighestRole(event.getSelfMember());
            Role target = roles.get(0);
            if (target.getPosition() >= highest.getPosition() || target.getPosition() >= bot.getPosition()) {
                event.reply("I can't assign this role because it is higher or equal to your (or my) highest role!");
                return;
            }

            // Remove role from member and call it a day
            event.getGuild().removeRoleFromMember(member, target).queue(
                e -> event.reply("I have successfully removed `" + rolename + "` from " + member.getEffectiveName() + "!"),
                f -> event.replyError("I was unable to take that role from the user. Is my role high enough?")
            );
        }
    }

    private class CloneRoleSubCommand extends Command {
        public CloneRoleSubCommand() {
            this.name = "clone";
            this.guildOnly = true;
            this.botPermissions = new Permission[]{Permission.MANAGE_ROLES};
            this.userPermissions = new Permission[]{Permission.MANAGE_ROLES};
        }

        @Override
        protected void execute(CommandEvent event) {
            // Grab input and parse for old role and new name
            String[] args = event.getArgs().split(" ");
            if (args.length < 2) {
                event.reply("Missing one or more arguments for this command. You need: role ID and new role name.");
                return;
            }

            Role oldRole;
            try {
                oldRole = event.getGuild().getRoleById(args[0]);
            } catch (NumberFormatException e) {
                event.reply("First argument must be a role ID!");
                return;
            }

            if (oldRole == null) {
                event.reply("Could not find specified role!");
                return;
            }

            // Get author's highest role for hierarchy checking
            Role highest = getHighestRole(event.getMember());
            if (oldRole.getPosition() >= highest.getPosition()) {
                event.reply("I can't clone this role because it is higher or equal to your highest role!");
                return;
            }

            List<String> missingPerms = new ArrayList<>();
            for (Permission perm : oldRole.getPermissions()) {
                if (event.getSelfMember().hasPermission(perm)) {
                    continue;
                }

                missingPerms.add(perm.getName());
            }

            if (!missingPerms.isEmpty()) {
                event.reply("Could not clone role because I am missing the following permissions: " + String.join(", ", missingPerms));
                return;
            }

            event.getGuild().createRole()
                .setName(event.getArgs().replace(args[0], "").trim())
                .setPermissions(oldRole.getPermissionsRaw())
                .setColor(oldRole.getColor())
                .queue(role -> event.reply("Created role successfully!"), throwable -> event.reply("Could not create role! " + throwable.getMessage()));
        }
    }

    /**
     * Gets the highest role for a member
     * @param member the member
     * @return the highest role this user has
     */
    public Role getHighestRole(Member member) {
        Role role = member.getGuild().getPublicRole();
        if (!member.getRoles().isEmpty()) {
            role = member.getRoles().get(0);
        }
        return role;
    }
}
