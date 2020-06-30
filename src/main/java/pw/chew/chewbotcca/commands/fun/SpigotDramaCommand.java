package pw.chew.chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

public class SpigotDramaCommand extends Command {

    public SpigotDramaCommand() {
        this.name = "spigotdrama";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        String response = new JSONObject(RestClient.get("https://chew.pw/api/spigotdrama")).getString("response");
        commandEvent.reply(response);
    }
}
