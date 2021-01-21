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
package pw.chew.chewbotcca.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;

import java.util.ArrayList;
import java.util.List;

public class DiffCommand extends Command {
    public DiffCommand() {
        this.name = "diff";
        this.aliases = new String[]{"compare"};
        this.guildOnly = true;
        this.cooldown = 5;
        this.cooldownScope = CooldownScope.USER;
        this.children = new Command[]{new CompareRolesSubCommand()};
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split(" ");
        if (args.length != 3) {
            event.reply("Invalid amount of arguments specified. Example: " + event.getPrefix() + "diff ROLE 708085624514543728 134445052805120001");
            return;
        }

        event.reply("Invalid comparison type specified. Valid: `ROLE`, `CHANNEL`, `MEMBER`");
    }

    /**
     * Compares roles
     */
    private static class CompareRolesSubCommand extends Command {

        public CompareRolesSubCommand() {
            this.name = "role";
            this.guildOnly = true;
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        }

        @Override
        protected void execute(CommandEvent event) {
            String[] args = event.getArgs().split(" ");

            if (args.length != 2) {
                event.reply("Invalid number of arguments provided. Please provide valid role IDs for this server.");
                return;
            }

            Role base = event.getGuild().getRoleById(args[0]);
            if (base == null) {
                event.reply("Unable to find base (first) role. Please ensure you are providing a valid ID to compare.");
                return;
            }
            Role compare = event.getGuild().getRoleById(args[1]);
            if (compare == null) {
                event.reply("Unable to find compare (second) role. Please ensure you are providing a valid ID to compare.");
                return;
            }

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Comparing roles");
            embed.setDescription("Comparing name, color, permissions, and other information.\n" +
                "Base role: " + args[0] + "\n" + "Compare role: " + args[1]);

            if (base.getName().equals(compare.getName())) {
                embed.addField("Name", "*Nothing to compare. Names are identical*", true);
            } else {
                String name = args[0] + ": " + base.getName() + "\n" +
                    args[1] + ": " + compare.getName();
                embed.addField("Name", name, true);
            }

            if (base.getColor() == compare.getColor()) {
                embed.addField("Color", "*Nothing to compare. Colors are identical*", true);
            } else {
                String color = args[0] + ": " + base.getColor() + "\n" +
                    args[1] + ": " + compare.getColor();
                embed.addField("Color", color, true);
            }

            if (base.getPermissionsRaw() == compare.getPermissionsRaw()) {
                embed.addField("Permissions", "*Nothing to compare. Permissions are identical*", false);
            } else {
                List<Permission> baseOnly = new ArrayList<>();
                List<Permission> compareOnly = new ArrayList<>();

                for (Permission perm : base.getPermissions()) {
                    if (!compare.getPermissions().contains(perm)) {
                        baseOnly.add(perm);
                    }
                }

                for (Permission perm : compare.getPermissions()) {
                    if (!base.getPermissions().contains(perm)) {
                        compareOnly.add(perm);
                    }
                }

                List<String> perms = new ArrayList<>();
                perms.add("""
                    + means compare role has the permission, and base doesn't.
                    - means compare role doesn't have the perm, but base does.
                    ```diff""");

                for (Permission perm : compareOnly) {
                    perms.add("+ " + perm.getName());
                }

                for (Permission perm : baseOnly) {
                    perms.add("- " + perm.getName());
                }

                perms.add("```");

                embed.addField("Permissions", String.join("\n", perms), false);
            }

            event.reply(embed.build());
        }
    }
}
