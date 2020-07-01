package pw.chew.chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

public class SpigotDramaCommand extends Command {

    public SpigotDramaCommand() {
        this.name = "spigotdrama";
        this.guildOnly = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        JSONObject response = new JSONObject(RestClient.get("https://chew.pw/api/spigotdrama"));
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("md678685", "https://github.com/md678685/spigot-drama-generator", "https://avatars0.githubusercontent.com/u/1917406");
        embed.setTitle("Spigot Drama Generator", "https://drama.essentialsx.net/");
        embed.setDescription(response.getString("response") + "\n\n" + "[Permalink](" + response.getString("permalink") + ")");

        commandEvent.reply(embed.build());
    }
}
