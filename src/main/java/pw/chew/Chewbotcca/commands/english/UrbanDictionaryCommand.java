package pw.chew.Chewbotcca.commands.english;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.chew.Chewbotcca.util.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UrbanDictionaryCommand extends Command {

    public UrbanDictionaryCommand() {
        this.name = "urban";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if(event.getChannelType() == ChannelType.TEXT && !toTextChannel(event.getChannel()).isNSFW()) {
            event.reply("This command is a little bit too edgy and may only be ran in NSFW channels or DMs. Sorry!");
            return;
        }

        String word = event.getArgs();
        JSONObject parse = new JSONObject(RestClient.get("http://api.urbandictionary.com/v0/define?term=" + URLEncoder.encode(word, StandardCharsets.UTF_8)));
        JSONArray list = parse.getJSONArray("list");
        if (list.length() == 0) {
            event.reply("No results found for term `" + word + "`!");
            return;
        }
        JSONObject info = list.getJSONObject(0);
        String definition = info.getString("definition").replace("\n", " ");
        int up = info.getInt("thumbs_up");
        int down = info.getInt("thumbs_down");
        String author = info.getString("author");
        String example = info.getString("example").replace("\n", " ");
        int total = up + down;
        float ratio = ((float)up / (float)total * 100);
        word = info.getString("word");
        String url = info.getString("permalink");
        event.reply(new EmbedBuilder()
                .setTitle("Urban Dictionary definition for **" + word + "**")
                .addField("Definition", definition, false)
                .addField("Author", author, true)
                .addField("Rating", "**" + up + "** 👍 - **" + down + "** 👎 (**" + ratio + "%**)", true)
                .addField("Example", example, false)
                .addField("URL", url, false)
                .build()
        );

    }

    public TextChannel toTextChannel(MessageChannel channel) {
        return (TextChannel)channel;
    }
}
