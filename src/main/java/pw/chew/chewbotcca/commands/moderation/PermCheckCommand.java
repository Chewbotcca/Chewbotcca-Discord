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
