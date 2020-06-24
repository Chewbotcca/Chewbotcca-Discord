package pw.chew.Chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.json.JSONObject;
import pw.chew.Chewbotcca.util.RestClient;

public class CatFactCommand extends Command {

    public CatFactCommand() {
        this.name = "catfact";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        String fact = new JSONObject(RestClient.get("https://catfact.ninja/fact")).getString("fact");
        commandEvent.reply(fact);
    }
}