package pw.chew.Chewbotcca.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
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
        String arg = event.getArgs().replace(" ", "");
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

