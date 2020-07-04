package pw.chew.chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Random;

public class RedditCommand extends Command {

    public RedditCommand() {
        this.name = "reddit";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        String baseUrl = "http://reddit.com/r/%s/.json";
        String shortUrl = "http://redd.it/";
        String url;
        int num = -1;
        if(commandEvent.getArgs().length() == 0) {
            url = "https://reddit.com/.json";
        } else {
            String[] args = commandEvent.getArgs().split(" ");
            url = String.format(baseUrl, args[0]);
            if(args.length > 1) {
                num = Integer.parseInt(args[1]);
            }
        }

        JSONObject reddit = new JSONObject(RestClient.get(url));
        JSONArray data = reddit.getJSONObject("data").getJSONArray("children");

        JSONObject post;

        if(num >= 0 && data.length() >= num) {
            post = data.getJSONObject(num);
        } else if(num >= 0) {
            commandEvent.reply("Number too large! Pick a number between 1 and " + data.length());
            return;
        } else {
            post = getRandom(data);
        }

        JSONObject postData = post.getJSONObject("data");

        if(postData.getBoolean("over_18") && commandEvent.getChannelType() == ChannelType.TEXT && !commandEvent.getTextChannel().isNSFW()) {
            commandEvent.reply("This post is marked as NSFW and must be ran in a NSFW channel!");
            return;
        }

        commandEvent.reply(generatePostEmbed(postData).build());
    }

    public EmbedBuilder generatePostEmbed(JSONObject post) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(post.getString("title"), post.getString("url"));
        embed.setAuthor(post.getString("author"), "https://reddit.com/u/" + post.getString("author"));
        embed.setTimestamp(Instant.ofEpochSecond(post.getLong("created_utc")));
        embed.addField("Upvotes", String.valueOf(post.getInt("score")), true);
        embed.addField("Comments", String.valueOf(post.getInt("num_comments")), true);
        if(post.has("description"))
            embed.setDescription(post.getString("description"));
        return embed;
    }

    public JSONObject getRandom(JSONArray array) {
        int rnd = new Random().nextInt(array.length());
        return array.getJSONObject(rnd);
    }
}
