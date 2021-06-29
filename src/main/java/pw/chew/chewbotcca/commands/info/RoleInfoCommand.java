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
package pw.chew.chewbotcca.commands.info;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import pw.chew.chewbotcca.util.JDAUtilUtil;
import pw.chew.chewbotcca.util.Mention;
import pw.chew.chewbotcca.util.ResponseHelper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

// %^rinfo command
public class RoleInfoCommand extends SlashCommand {

    public RoleInfoCommand() {
        this.name = "roleinfo";
        this.help = "Find some information about roles";
        this.aliases = new String[]{"rinfo"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION};
        this.guildOnly = true;
        this.options = Arrays.asList(
            new OptionData(OptionType.ROLE, "role", "The role to get info about").setRequired(true),
            new OptionData(OptionType.STRING, "mode", "The type of data to receive")
                .addChoice("General Info", "general")
                .addChoice("Members", "members")
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String mode = ResponseHelper.guaranteeStringOption(event, "mode", "general");

        // "Nullable" begone
        if (event.getGuild() == null || event.getMember() == null) return;

        Role role = event.getOptionsByName("role").get(0).getAsRole();

        // Make a response depending on the mode
        if (mode.equals("members")) {
            // Send message then edit it
            event.replyEmbeds(new EmbedBuilder().setDescription("Gathering members...").build()).queue(interactionHook -> {
                interactionHook.retrieveOriginal().queue(message -> {
                    gatherMembersInfo(event.getGuild(), role, false, event.getUser()).paginate(message, 1);
                });
            });
        } else {
            event.replyEmbeds(gatherMainInfo(event.getGuild(), role, event.getMember()).build()).queue();
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        // Get the args
        String arg = event.getArgs();
        if (arg.isEmpty()) {
            event.reply("Please specify a role name to look up!");
            return;
        }

        String mode = "";

        // Set the mode if necessary
        if(arg.contains("members")) {
            mode = "members";
            arg = arg.replace("--members", "").trim();
        }

        boolean mention = false;
        if(arg.contains("--mention")) {
            mention = true;
            arg = arg.replace("--mention", "").trim();
        }

        // Parse and find the role
        Role role = null;
        boolean id;
        try {
            Long.parseLong(arg);
            id = true;
        } catch (NumberFormatException e) {
            id = false;
        }
        if(arg.contains("<")) {
            role = (Role) Mention.parseMention(arg, event.getGuild(), event.getJDA());
        } else if(id) {
            role = event.getGuild().getRoleById(arg);
        } else {
            List<Role> roles = event.getGuild().getRolesByName(arg, true);
            if(roles.size() > 0) {
                role = roles.get(0);
            }
        }
        if(role == null) {
            event.reply("No roles found for the given input.");
            return;
        }

        // Make a response depending on the mode
        if(mode.equals("members")) {
            gatherMembersInfo(event.getGuild(), role, mention, event.getAuthor()).paginate(event.getChannel(), 1);
        } else {
            event.reply(gatherMainInfo(event.getGuild(), role, event.getMember()).build());
        }
    }

    /**
     * Gather main role info
     *
     * @param server the server to get the role from
     * @param role   the role
     * @param author the author of the message for perm checks
     * @return an embed to be build
     */
    public EmbedBuilder gatherMainInfo(Guild server, Role role, Member author) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Role Information for: " + role.getName());
        // Get the member counts
        int members = server.getMembersWithRoles(role).size();
        int total = server.getMemberCount();
        DecimalFormat df = new DecimalFormat("#.##");
        String percent = df.format((float) members / (float) total * 100);
        // Return the member count
        embed.addField("Members", NumberFormat.getNumberInstance(Locale.US).format(members) + " / " + NumberFormat.getNumberInstance(Locale.US).format(total) + " (" + percent + "%)", true);
        embed.addField("Mention / ID", role.getAsMention() + "\n" + role.getId(), true);
        // Find and provide info
        List<String> info = new ArrayList<>();
        info.add(getInfoFormat(role.isHoisted(), "Hoisted"));
        info.add(getInfoFormat(role.isMentionable(), "Mentionable"));
        info.add(getInfoFormat(role.getTags().isBot(), "Bot Role"));
        info.add(getInfoFormat(role.getTags().isBoost(), "Boost Role"));
        info.add(getInfoFormat(role.getTags().isIntegration(), "Integration Role"));
        embed.addField("Information", String.join("\n", info), true);
        embed.setColor(role.getColor());
        embed.setFooter("Created");
        // If the user has permission to manage roles, show the permissions
        if (author.hasPermission(Permission.MANAGE_ROLES)) {
            Permission[] perms = role.getPermissions().toArray(new Permission[0]);
            Set<Permission> temp = new TreeSet<>(Arrays.asList(perms));
            String description = "";
            if (!role.isPublicRole()) {
                Permission[] every = server.getPublicRole().getPermissions().toArray(new Permission[0]);
                Arrays.asList(every).forEach(temp::remove);
                description = "Elevated permissions (perms this role has that everyone role doesn't)\n\n";
            }
            embed.setDescription(description + generatePermissionList(temp));
        }
        embed.setTimestamp(role.getTimeCreated());

        return embed;
    }

    /**
     * Gather members of a role
     *
     * @param server  the server to get role from
     * @param role    the role
     * @param mention should render mentions or not
     */
    public Paginator gatherMembersInfo(Guild server, Role role, boolean mention, User author) {
        if (role == server.getPublicRole()) {
            throw new IllegalArgumentException("Finding all members is unsupported.");
        }
        Paginator.Builder paginator = JDAUtilUtil.makePaginator().clearItems();
        paginator.setText("Members in role " + role.getName());
        // Get the member list and find how members actually with the role
        List<Member> memberList = server.getMemberCache().asList();
        for (Member member : memberList) {
            if (member.getRoles().contains(role)) {
                if (mention)
                    paginator.addItems(member.getAsMention());
                else
                    paginator.addItems(member.getUser().getAsTag());
            }
        }
        if (paginator.getItems().isEmpty())
            paginator.addItems("No one has this role!");

        return paginator.setUsers(author).build();
    }

    /**
     * Generate a fancy permission list
     * @param permissions the permissions
     * @return a permission list
     */
    public String generatePermissionList(Set<Permission> permissions) {
        ArrayList<CharSequence> perms = new ArrayList<>();
        Permission[] permList = permissions.toArray(new Permission[0]);
        if (permissions.isEmpty()) {
            return "*No perms selected*";
        }
        for(int i = 0; i < permissions.size(); i++) {
            perms.add(permList[i].getName());
        }
        return String.join(", ", perms);
    }

    /**
     * Helper method for %^rinfo info formatting
     * @param yes if it's green or not
     * @param string the string to append
     * @return a formatted string
     */
    private String getInfoFormat(boolean yes, String string) {
        if (yes) {
            return "\uD83D\uDFE2 " + string;
        } else {
            return "\uD83D\uDD34 " + string;
        }
    }
}

