package pw.chew.Chewbotcca.commands.quotes;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.json.JSONArray;
import pw.chew.Chewbotcca.util.RestClient;

public class TRBMBCommand extends Command {

    public TRBMBCommand() {
        this.name = "trbmb";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply(new JSONArray(RestClient.get("https://api.chew.pro/trbmb")).getString(0));
    }
}
