/*
 * Copyright (C) 2021 Chewbotcca
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
package pw.chew.chewbotcca.commands.services;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;
import pw.chew.jdachewtils.command.OptionHelper;

import java.time.Instant;
import java.util.Arrays;
import java.util.Random;

// %^reddit command
public class RedditCommand extends SlashCommand {

    public RedditCommand() {
        this.name = "reddit";
        this.help = "Searches for and returns a post from reddit";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.options = Arrays.asList(
            new OptionData(OptionType.INTEGER, "number", "The post number to grab").setRequired(true),
            new OptionData(OptionType.STRING, "subreddit", "The subreddit to get a post from")
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        int num = (int) event.getOptionsByName("number").get(0).getAsLong();
        String subreddit = OptionHelper.optString(event, "subreddit", "");
        boolean nsfwAllowed = event.getChannelType() == ChannelType.TEXT && !event.getTextChannel().isNSFW();

        try {
            event.replyEmbeds(gatherData(num, subreddit, nsfwAllowed)).queue();
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        String[] args = commandEvent.getArgs().split(" ");
        boolean nsfwAllowed = commandEvent.getChannelType() == ChannelType.TEXT && !commandEvent.getTextChannel().isNSFW();

        int num = -1;
        String subreddit = "";
        switch (args.length) {
            case 0:
                commandEvent.replyWarning("This command requires at least 1 argument! Post number and subreddit (optional)");
                return;
            case 1:
                try {
                    num = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    commandEvent.replyWarning("First argument must be a number!");
                    return;
                }
            case 2:
                subreddit = args[1];
            default:
        }

        try {
            commandEvent.reply(gatherData(num, subreddit, nsfwAllowed));
        } catch (IllegalArgumentException e) {
            commandEvent.replyWarning(e.getMessage());
        }
    }

    private MessageEmbed gatherData(int num, String subreddit, boolean nsfwAllowed) {
        String baseUrl = "https://reddit.com/r/%s/.json";
        String url;
        // If no args, just use the front page json
        if (subreddit.isBlank()) {
            url = "https://reddit.com/.json";
        } else {
            // Otherwise, a subreddit was probably specified, so use that
            url = String.format(baseUrl, subreddit.replace("r/", ""));
        }

        // Get data from reddit
        JSONObject reddit = new JSONObject(RestClient.get(url));
        JSONArray data = reddit.getJSONObject("data").getJSONArray("children");

        if (data.length() == 0) {
            throw new IllegalArgumentException("This sub-reddit does not have any posts!");
        }

        JSONObject post;

        // If the specified post is greater than the actual amount of posts
        if (num >= 0 && data.length() >= num) {
            post = data.getJSONObject(num);
        } else if (num >= 0) {
            throw new IllegalArgumentException("Number too large! Pick a number between 1 and " + data.length());
        } else {
            post = getRandom(data);
        }

        // Get the post data
        JSONObject postData = post.getJSONObject("data");

        // If it's over 18 and it's not an NSFW channel, don't show it
        if (postData.getBoolean("over_18") && nsfwAllowed) {
            throw new IllegalArgumentException("This post is marked as NSFW and must be ran in a NSFW channel!");
        }

        return generatePostEmbed(postData).build();
    }

    // Code to generate a post embed based off of a post json object
    public EmbedBuilder generatePostEmbed(JSONObject post) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(post.getString("title"), "https://reddit.com/" + post.getString("permalink"));
        embed.setAuthor("u/" + post.getString("author"), "https://reddit.com/u/" + post.getString("author"));
        embed.setTimestamp(Instant.ofEpochSecond(post.getLong("created_utc")));
        embed.addField("Upvotes", String.valueOf(post.getInt("score")), true);
        embed.addField("Comments", String.valueOf(post.getInt("num_comments")), true);
        if(post.has("description"))
            embed.setDescription(post.getString("description"));
        if (post.has("preview"))
            embed.setImage(post.getString("url"));
        return embed;
    }

    // Method to get a random JSONObject from a JSONArray
    public JSONObject getRandom(JSONArray array) {
        int rnd = new Random().nextInt(array.length());
        return array.getJSONObject(rnd);
    }
}
