package pw.chew.chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

public class CatCommand extends Command {

    public CatCommand() {
        this.name = "cat";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        try {
            String showcat = new JSONObject(RestClient.get("http://aws.random.cat/meow")).getString("file");

            commandEvent.reply(new EmbedBuilder()
                    .setTitle("Adorable.", showcat)
                    .setImage(showcat)
                    .build()
            );
        } catch (JSONException e) {
            commandEvent.reply(new EmbedBuilder()
                    .setTitle("Sad mews!")
                    .setDescription("The Cat API is not working. :( Try again later? :3")
                    .build()
            );
        }
    }
}