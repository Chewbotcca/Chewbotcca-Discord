package pw.chew.Chewbotcca.commands.quotes;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.json.JSONObject;
import pw.chew.Chewbotcca.util.RestClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class AcronymCommand extends Command {

    public AcronymCommand() {
        this.name = "acronym";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            event.reply("Acronym for " + event.getArgs() + " is " + new JSONObject(RestClient.get("https://api.chew.pro/acronym/" + URLEncoder.encode(event.getArgs(), "UTF-8"))).getString("phrase"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            event.reply("Invalid encoding error? How did this happen...");
        }
    }
}
