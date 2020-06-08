package pw.chew.Chewbotcca.commands;

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
import java.util.concurrent.*;

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
        String arg = event.getArgs().replace(" ", "");
        Role role = null;
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
            List<Role> roles = event.getGuild().getRolesByName(event.getArgs(), true);
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

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Role Information for: " + role.getName());
        List<User> memberList = new ArrayList<>();
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
        embed.addField("Mention", role.getAsMention(), true);
        String info = "";
        if(role.isHoisted()) {
            info = "\uD83D\uDFE2 Hoisted\n";
        } else {
            info = "\uD83D\uDD34 Hoisted\n";
        }
        if(role.isMentionable()) {
            info += "\uD83D\uDFE2 Mentionable";
        } else {
            info += "\uD83D\uDD34 Mentionable";
        }
        embed.addField("Information", info, true);
        embed.setColor(role.getColor());
        embed.setFooter("Created");
        embed.setDescription(generatePermissionList(role.getPermissions()));
        embed.setTimestamp(role.getTimeCreated());
        event.reply(embed.build());
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

