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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import pw.chew.chewbotcca.util.ArgsParser;

import java.util.ArrayList;
import java.util.List;

public class DiffCommand extends Command {
    public DiffCommand() {
        this.name = "diff";
        this.aliases = new String[]{"compare"};
        this.guildOnly = true;
        this.cooldown = 5;
        this.cooldownScope = CooldownScope.USER;
        this.children = new Command[]{new CompareRolesSubCommand(), new CompareMembersSubCommand()};
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
     * Compares roles.
     * Takes as input 2 role IDs.  Example:  diff role 708085624514543728 134445052805120001
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


            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Comparing roles")
                    .setDescription("Comparing name, color, permissions, and other information.\n" +
                            "Base role: " + base.getName() + " ( " + base.getId() + " ) " + "\n" +
                            "Compare role: " + compare.getName() + " ( " + compare.getId() + " ) ");

            if (base.getName().equals(compare.getName())) {
                embed.addField("Name", "*Nothing to compare.\n Names are identical.*", true);
            } else {
                String name = base.getName() + "\n" +
                        compare.getName();
                embed.addField("Name", name, true);
            }


            if (base.getColor() == compare.getColor()) {
                embed.addField("Color", "*Nothing to compare.\n Colors are identical.*", true);
            } else {
                String baseHex = colorToHex(base.getColor().getRed(), base.getColor().getGreen(), base.getColor().getBlue());
                String compareHex = colorToHex(compare.getColor().getRed(), compare.getColor().getGreen(), compare.getColor().getBlue());
                String color = (base.getColorRaw() == Role.DEFAULT_COLOR_RAW ? "Default color" : baseHex) + "\n" +
                        (compare.getColorRaw() == Role.DEFAULT_COLOR_RAW ? "Default color" : compareHex);
                embed.addField("Color", color, true);
            }
            embed.addBlankField(false);

            // compare info section

            String information = """
                    Hoisted
                    Mentionable
                    Bot role
                    Boost role
                    Integration role""";
            embed.addField("Information", information, true);

            List<String> baseInfo = new ArrayList<>();
            baseInfo.add(getInfoState(base.isHoisted()));
            baseInfo.add(getInfoState(base.isMentionable()));
            baseInfo.add(getInfoState(base.getTags().isBot()));
            baseInfo.add(getInfoState(base.getTags().isBoost()));
            baseInfo.add(getInfoState(base.getTags().isIntegration()));

            embed.addField(base.getName(), String.join("\n", baseInfo), true);

            List<String> compareInfo = new ArrayList<>();
            compareInfo.add(getInfoState(compare.isHoisted()));
            compareInfo.add(getInfoState(compare.isMentionable()));
            compareInfo.add(getInfoState(compare.getTags().isBot()));
            compareInfo.add(getInfoState(compare.getTags().isBoost()));
            compareInfo.add(getInfoState(compare.getTags().isIntegration()));

            embed.addField(compare.getName(), String.join("\n", compareInfo), true);


            if (base.getPermissionsRaw() == compare.getPermissionsRaw()) {
                embed.addField("✅ Permissions 🚫", "*Nothing to compare.\n Permissions are identical*", false);
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

                embed.addField("✅ Permissions 🚫", String.join("\n", perms), false);
            }

            event.reply(embed.build());
        }

        /**
         * Helper method for %^diff role command about a role's info state.
         *
         * @param yes if it's green or not
         * @return a green sign if true, red if false
         */
        private String getInfoState(boolean yes) {
            if (yes) {
                return "\uD83D\uDFE2";
            } else {
                return "\uD83D\uDD34";
            }
        }

        /**
         * Source: https://stackoverflow.com/questions/3607858/convert-a-rgb-color-value-to-a-hexadecimal-string
         * Function that converts RGB values to hexadecimal code.
         *
         * @param red   red value of color
         * @param green green value of color
         * @param blue  blue value of color
         * @return the hexadecimal code of the color
         */
        private String colorToHex(int red, int green, int blue) {
            return String.format("#%02x%02x%02x", red, green, blue);
        }
    }

    /**
     * Compares members
     * Takes as input 2 mentioned members. Example diff member @random @random2
     */
    private static class CompareMembersSubCommand extends Command {

        public CompareMembersSubCommand() {
            this.name = "member";
            this.guildOnly = true;
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        }

        @Override
        protected void execute(CommandEvent event) {
            List<Member> members;

            try {
                members = ArgsParser.parseMembers(event.getArgs(), 2, event.getMessage());
            } catch (IllegalArgumentException e) {
                event.reply("""
                        Invalid number of arguments provided.
                        Please provide valid members.
                        Example : diff member @random @random2.""");
                return;
            }

            Member base = members.get(0);
            Member compare = members.get(1);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Comparing members")
                    .setDescription("Comparing time joined, time boosted, roles and other information.\n" +
                            "Base member: " + base.getUser().getAsTag() + "\n" +
                            "Compare member: " + compare.getUser().getAsTag());

            // Dates of arrival in server
            String date = base.getTimeJoined().toString().substring(0, 10) + "\n" + compare.getTimeJoined().toString().substring(0, 10);
            embed.addField("Date Joined", date, true);
            embed.addBlankField(false);

            // Status comparison
            String onlineStatus = base.getUser().getAsTag() + ":   " + base.getOnlineStatus().name() + "\n" +
                    compare.getUser().getAsTag() + ":   " + compare.getOnlineStatus().name();
            if (base.getOnlineStatus().equals(compare.getOnlineStatus())) {
                embed.addField("Status", "Nothing to compare.", true);
            } else {
                embed.addField("Status", onlineStatus, true);
            }

            // Starting date of boosting time for a member.
            String baseBoostingTime;
            String compareBoostingTime;
            if (base.getTimeBoosted() != null) {
                // keeps yyyy-mm-dd
                baseBoostingTime = "Boosting since: " + base.getTimeBoosted().toString().substring(0, 10);
            } else {
                baseBoostingTime = "Not boosting";
            }

            if (compare.getTimeBoosted() != null) {
                // keeps yyyy-mm-dd
                compareBoostingTime = "Boosting since: " + compare.getTimeBoosted().toString().substring(0, 10);
            } else {
                compareBoostingTime = "Not boosting";
            }

            embed.addField("Server Boosting", baseBoostingTime + "\n" + compareBoostingTime, true);
            embed.addBlankField(false);

            // Roles comparison of members
            List<String> baseRoles = new ArrayList<>();
            List<String> compareRoles = new ArrayList<>();

            baseRoles.add("""
                    ```diff""");
            compareRoles.add("""
                    ```diff""");
            for (Role role : base.getRoles()) {
                baseRoles.add(role.getName());
            }

            for (Role role : compare.getRoles()) {
                compareRoles.add(role.getName());
            }

            baseRoles.add("```");
            compareRoles.add("```");

            embed.addField(base.getUser().getName() + "' Roles ", String.join("\n", baseRoles), true);
            embed.addField(compare.getUser().getName() + "' Roles ", String.join("\n", compareRoles), true);

            event.reply(embed.build());
        }
    }
}
