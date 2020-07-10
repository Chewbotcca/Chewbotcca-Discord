/*
 * Copyright (C) 2020 Chewbotcca
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
import java.util.Random;

// %^reddit command
public class RedditCommand extends Command {

    public RedditCommand() {
        this.name = "reddit";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        String baseUrl = "http://reddit.com/r/%s/.json";
        String url;
        int num = -1;
        // If no args, just use the front page json
        if(commandEvent.getArgs().length() == 0) {
            url = "https://reddit.com/.json";
        } else {
            // Otherwise, a subreddit was probably specified, so use that
            String[] args = commandEvent.getArgs().split(" ");
            url = String.format(baseUrl, args[0]);
            if(args.length > 1) {
                num = Integer.parseInt(args[1]);
            }
        }

        // Get data from reddit
        JSONObject reddit = new JSONObject(RestClient.get(url));
        JSONArray data = reddit.getJSONObject("data").getJSONArray("children");

        JSONObject post;

        // If the specified post is greater than the actual amount of posts
        if(num >= 0 && data.length() >= num) {
            post = data.getJSONObject(num);
        } else if(num >= 0) {
            commandEvent.reply("Number too large! Pick a number between 1 and " + data.length());
            return;
        } else {
            post = getRandom(data);
        }

        // Get the post data
        JSONObject postData = post.getJSONObject("data");

        // If it's over 18 and it's not an NSFW channel, don't show it
        if(postData.getBoolean("over_18") && commandEvent.getChannelType() == ChannelType.TEXT && !commandEvent.getTextChannel().isNSFW()) {
            commandEvent.reply("This post is marked as NSFW and must be ran in a NSFW channel!");
            return;
        }

        commandEvent.reply(generatePostEmbed(postData).build());
    }

    // Code to generate a post embed based off of a post json object
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

    // Method to get a random JSONObject from a JSONArray
    public JSONObject getRandom(JSONArray array) {
        int rnd = new Random().nextInt(array.length());
        return array.getJSONObject(rnd);
    }
}
