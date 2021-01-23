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
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.awt.*;
import java.util.*;
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
            event.reply("Invalid amount of arguments specified. Example: " + event.getPrefix() + "diff role moderator admin");
            return;
        }

        event.reply("Invalid comparison type specified. Valid: `ROLE`, `CHANNEL`, `MEMBER`");
    }

    /**
     * Compares roles.
     * Takes 2 String arguments that have to be roles.  Example:  diff role moderator admin
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
                event.reply("Invalid number of arguments provided. Please provide valid role names.");
                return;
            }

            String baseString = args[0].toLowerCase();
            String compareString = args[1].toLowerCase();
            Role base = null;
            Role compare = null;

            // iterate through roles to check if base and compare are actual roles.
            for (Role role : event.getGuild().getRoles()){
                if(role.getName().toLowerCase().equals(baseString)){
                    base = role;
                }
                if(role.getName().toLowerCase().equals(compareString)){
                    compare = role;
                }
            }
            if (base == null) {
                event.reply("Unable to find base (first) role. Please ensure you are providing a valid role name.");
                return;
            }
            if (compare == null) {
                event.reply("Unable to find compare (second) role. Please ensure you are providing a valid role to compare.");
                return;
            }


            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Comparing roles")
                    .setDescription("Comparing name, color, permissions, and other information.\n" +
                        "Base role: " + base.getName()  + "\n" + "Compare role: " + compare.getName())
                    .setColor(new Color(96, 70, 184));
            try {
                embed.setThumbnail("https://c1.wallpaperflare.com/preview/634/485/143/analysis-antique-background-balance.jpg");
            }catch (Exception e){
                Sentry.captureException(e);
            }

            // compare names
            if (base.getName().equals(compare.getName())) {
                embed.addField("Try again with different roles", "*Nothing to compare. Roles are identical*", true);
                event.reply(embed.build());
                return;
            } else {
                String name = base.getName() + "\n" +
                        compare.getName();
                embed.addField("Name ", name, true);
            }

            // compare colors ( RGB values )
            if (base.getColorRaw() == compare.getColorRaw()) {
                embed.addField("Color 🔴🟠🟡🟢🔵🟣🟤⚫⚪", "*Nothing to compare. Colors are identical*", true);
            } else {
                String color = base.getColor()+ "\n" +
                    compare.getColor();
                embed.addField("Color 🔴🟠🟡🟢🔵🟣🟤⚫⚪", color, true);
            }

            // compare permissions
            if (base.getPermissionsRaw() == compare.getPermissionsRaw()) {
                embed.addField("✅ Permissions 🚫", "*Nothing to compare. Permissions are identical*", false);
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
                String start = """
                    + means compare role has the permission, and base doesn't.
                    - means compare role doesn't have the perm, but base does.
                     ```diff""".replaceAll("compare",compare.getName()).replaceAll("base",base.getName());
                perms.add(start);
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
    }

    /**
     * Compares users
     * Takes 2 arguments of type userIdTag. Example: diff member Query#1234 random#4567
     */
    private static class CompareMembersSubCommand extends Command{

        public CompareMembersSubCommand() {
            this.name = "member";
            this.guildOnly = true;
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        }

        @Override
        protected void execute(CommandEvent event) {
            String[] args = event.getArgs().split(" ");

            if (args.length != 2) {
                event.reply("Invalid number of arguments provided. Please provide valid member names.");
                return;
            }

            Member member1 = null;
            Member member2 = null;

            try {
                member1 = event.getGuild().getMemberByTag(args[0]);
                member2 = event.getGuild().getMemberByTag(args[1]);
            }catch (Exception e){
                Sentry.captureException(e);
            }

            if (member1 == null){
                event.reply("Unable to find first member. Please ensure you are providing a valid tag .");
                return;
            }
            if (member2 == null){
                event.reply("Unable to find second member. Please ensure you are providing a valid tag .");
                return;
            }


            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Comparing members")
                    .setDescription("Comparing Time joined,Time boosted , Roles, and other information.\n" +
                        "First member: " + member1.getUser().getName() + "\n" + "Second member : " + member2.getUser().getName())
                    .setColor(new Color(117, 46, 134));

                try {
                    embed.setThumbnail("https://c1.wallpaperflare.com/preview/634/485/143/analysis-antique-background-balance.jpg");
                }catch (Exception e){
                    Sentry.captureException(e);
                }


                if (member1.getUser().getAsTag().equals(member2.getUser().getAsTag())) {
                    embed.addField("Try again with different members", "*Nothing to compare. Members are identical*", true);
                    event.reply(embed.build());
                    return;
                } else {
                    String names = member1.getUser().getName() + "\n" +
                            member2.getUser().getName();
                    embed.addField("Name", names, true);
                }

                // Dates of arrival in server
                String date = member1.getTimeJoined().toString().substring(0,10) + "\n" +
                        member2.getTimeJoined().toString().substring(0,10);
                embed.addField("🛬 Date of Arrival 🛬", date, true);
                embed.addBlankField(false);
                // Status comparison
                String onlineStatus = member1.getUser().getName() + " :   " + member1.getOnlineStatus().name() + "\n" +
                        member2.getUser().getName() + " :   " + member2.getOnlineStatus().name();
                embed.addField("🟢 🌙 Status 💤 ⛔", onlineStatus, true);

                // Starting date of boosting time for a member.
                String boostingTime1;
                String boostingTime2;
                if (member1.getTimeBoosted() != null){
                    // keeps yyyy-mm-dd
                    boostingTime1 = member1.getTimeBoosted().toString().substring(0,10);
                }else{
                    boostingTime1 = "Currently not boosting";
                }

                if (member2.getTimeBoosted() != null){
                    // keeps yyyy-mm-dd
                    boostingTime2 = member2.getTimeBoosted().toString().substring(0,10);
                }else{
                    boostingTime2 = "Currently not boosting";
                }

                embed.addField("💎 Nitro Boosting 💎", boostingTime1 + "\n" + boostingTime2, true);

                embed.addBlankField(false);

                // Roles comparison of members
                List<String> member1Roles = new ArrayList<>();
                List<String> member2Roles = new ArrayList<>();

                member1Roles.add("""
                        ```diff""");
                member2Roles.add("""
                        ```diff""");
                for (Role role : member1.getRoles()){
                    member1Roles.add(role.getName());
                }

                for (Role role : member2.getRoles()){
                    member2Roles.add(role.getName());
                }

                member1Roles.add("```");
                member2Roles.add("```");

                embed.addField( member1.getUser().getName() + "' Roles 🐱‍👤", String.join("\n", member1Roles), true);
                embed.addField( member2.getUser().getName() + "' Roles 🐱‍👓", String.join("\n", member2Roles), true);


                event.reply(embed.build());

        }

    }
}
