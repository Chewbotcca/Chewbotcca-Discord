package pw.chew.Chewbotcca.commands.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class RoleInfoCommand extends Command {

    public RoleInfoCommand() {
        this.name = "roleinfo";
        this.aliases = new String[]{"rinfo"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        String arg = event.getArgs();

        String mode = "";

        if(arg.contains("members")) {
            mode = "members";
            arg = arg.replace(" members", "");
            arg = arg.replace("members ", "");
            arg = arg.replace("members", "");
        }

        Role role;
        boolean id;
        try {
            Long.parseLong(arg);
            id = true;
        } catch (NumberFormatException e) {
            id = false;
        }
        if(arg.contains("<")) {
            String roleId = arg.replace("<@&", "").replace(">", "");
            role = event.getGuild().getRoleById(roleId);
        } else if(id) {
            role = event.getGuild().getRoleById(arg);
        } else {
            List<Role> roles = event.getGuild().getRolesByName(arg, true);
            if(roles.size() > 0) {
                role = roles.get(0);
            } else {
                event.reply("No roles found for the given input.");
                return;
            }
        }
        if(role == null) {
            event.reply("No roles found for the given input.");
            return;
        }


        if(mode.equals("members")) {
            event.reply(gatherMembersInfo(event, role).build());
        } else {
            event.reply(gatherMaininfo(event, role).build());
        }
    }

    public EmbedBuilder gatherMaininfo(CommandEvent event, Role role) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Role Information for: " + role.getName());
        event.getChannel().sendTyping().queue();
        try {
            event.getGuild().retrieveMembers().get();
            await().atMost(30, TimeUnit.SECONDS).until(() -> event.getGuild().getMemberCache().size() == event.getGuild().getMemberCount());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        int members = event.getGuild().getMembersWithRoles(role).size();
        int total = event.getGuild().getMemberCount();
        DecimalFormat df = new DecimalFormat("#.##");
        String percent = df.format((float)members / (float)total * 100);
        embed.addField("Members", NumberFormat.getNumberInstance(Locale.US).format(members) + " / " + NumberFormat.getNumberInstance(Locale.US).format(total) + " (" + percent + "%)", true);
        embed.addField("Mention / ID", role.getAsMention() + "\n" + role.getId(), true);
        String info;
        if(role.isHoisted()) {
            info = "\uD83D\uDFE2 Hoisted\n";
        } else {
            info = "\uD83D\uDD34 Hoisted\n";
        }
        if(role.isMentionable()) {
            info += "\uD83D\uDFE2 Mentionable\n";
        } else {
            info += "\uD83D\uDD34 Mentionable\n";
        }
        if(role.isManaged()) {
            info += "\uD83D\uDFE2 Managed";
        } else {
            info += "\uD83D\uDD34 Managed";
        }
        embed.addField("Information", info, true);
        embed.setColor(role.getColor());
        embed.setFooter("Created");
        if(event.getMember().hasPermission(Permission.MANAGE_ROLES))
            embed.setDescription(generatePermissionList(role.getPermissions()));
        embed.setTimestamp(role.getTimeCreated());

        return embed;
    }

    public EmbedBuilder gatherMembersInfo(CommandEvent event, Role role) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Members in role " + role.getName());
        try {
            event.getGuild().retrieveMembers().get();
            await().atMost(30, TimeUnit.SECONDS).until(() -> event.getGuild().getMemberCache().size() == event.getGuild().getMemberCount());
        } catch (InterruptedException | ExecutionException interruptedException) {
            interruptedException.printStackTrace();
        }
        List<Member> memberList = event.getGuild().getMemberCache().asList();
        int added = 0;
        int total = 0;
        List<CharSequence> members = new ArrayList<>();
        for(Member member : memberList) {
            if(member.getRoles().contains(role)) {
                if (added <= 75) {
                    members.add(member.getAsMention());
                    added++;
                }
                total++;
            }
        }
        embed.setDescription(String.join("\n", members));
        embed.setFooter("Showing " + added + " out of " + total);
        return embed;
    }

    public String generatePermissionList(EnumSet<Permission> permissions) {
        ArrayList<CharSequence> perms = new ArrayList<>();
        Permission[] permList = permissions.toArray(new Permission[0]);
        for(int i = 0; i < permissions.size(); i++) {
            perms.add(permList[i].getName());
        }
        return String.join(", ", perms);
    }
}

