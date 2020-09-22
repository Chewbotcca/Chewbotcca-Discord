package pw.chew.chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

public class RoryCommand extends Command {

    public RoryCommand() {
        this.name = "rory";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        String showcat = new JSONObject(RestClient.get("https://rory.cat/purr")).getString("url");

        event.reply(new EmbedBuilder()
            .setTitle("Rory :3", showcat)
            .setImage(showcat)
            .build()
        );
    }
}
