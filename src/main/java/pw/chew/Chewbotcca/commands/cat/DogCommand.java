package pw.chew.Chewbotcca.commands.cat;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONObject;
import pw.chew.Chewbotcca.util.RestClient;

public class DogCommand extends Command {

    public DogCommand() {
        this.name = "dog";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        String dog = new JSONObject(RestClient.get("https://random.dog/woof.json")).getString("url");

        commandEvent.reply(new EmbedBuilder()
                .setTitle("Adorable.", dog)
                .setImage(dog)
                .build()
        );
    }
}