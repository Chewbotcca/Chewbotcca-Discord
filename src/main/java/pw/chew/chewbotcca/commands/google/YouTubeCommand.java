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
package pw.chew.chewbotcca.commands.google;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.RestClient;

import java.awt.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// %^youtube command
public class YouTubeCommand extends Command {

    public YouTubeCommand() {
        this.name = "youtube";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.cooldown = 5;
        this.cooldownScope = CooldownScope.USER;
    }

    @Override
    protected void execute(CommandEvent event) {
        // Get the input and find results
        String search = event.getArgs();
        String findidurl = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&q=" + URLEncoder.encode(search, StandardCharsets.UTF_8) + "&key=" + PropertiesManager.getGoogleKey();
        // Find a video if there is one
        String id;
        try {
            id = new JSONObject(RestClient.get(findidurl)).getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId");
        } catch (JSONException e) {
            event.reply("No videos found!");
            return;
        }
        // Get the video
        JSONObject url = new JSONObject(RestClient.get("https://www.googleapis.com/youtube/v3/videos?id=" + id + "&key=" + PropertiesManager.getGoogleKey() + "&part=snippet,contentDetails,statistics"));
        if (url.getJSONObject("pageInfo").getInt("totalResults") == 0) {
            event.reply("No results found.");
            return;
        }

        event.reply(response(url, id, event.getTextChannel()).build());
    }

    /**
     * Generate an embed for the specified video
     * @param url the json object from youtube
     * @param id the video id
     * @return an embedbuilder ready to be built
     */
    public EmbedBuilder response(JSONObject url, String id, TextChannel channel) {
        boolean restricted = url
            .getJSONArray("items")
            .getJSONObject(0)
            .getJSONObject("contentDetails")
            .getJSONObject("contentRating")
            .has("ytRating");

        // Don't respond if not nsfw channel
        if(restricted && !channel.isNSFW()) {
            return new EmbedBuilder()
                .setTitle("Uh oh! Too naughty!")
                .setDescription("The returned video is marked as age restricted by YouTube, and may only be shown in NSFW channels or DMs. Sorry!");
        }

        // Gather stats and info objects
        JSONObject stats = url.getJSONArray("items").getJSONObject(0).getJSONObject("statistics");
        JSONObject info = url.getJSONArray("items").getJSONObject(0).getJSONObject("snippet");
        // Get other stuff as well
        String length = url.getJSONArray("items").getJSONObject(0).getJSONObject("contentDetails").getString("duration");
        long views = 0;
        // The view count is apparently optional, as proven with Apple's WWDC 2020 livestream
        if(stats.has("viewCount"))
            views = Long.parseLong(stats.getString("viewCount"));
        int likes = Integer.parseInt(stats.getString("likeCount"));
        int dislike = Integer.parseInt(stats.getString("dislikeCount"));
        // Parse upload
        String upload = info.getString("publishedAt");
        DecimalFormat df = new DecimalFormat("#.##");
        float totallikes = likes + dislike;
        String percent = df.format(likes / totallikes * 100);
        String dispercent = df.format(dislike / totallikes * 100);

        String urlpls = "http://youtu.be/" + id;

        // Finally make the embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("YouTube Video Search");
        embed.setTitle(info.getString("title"), urlpls);
        embed.addField("Uploader", "[" + info.getString("channelTitle") + "](https://youtube.com/channel/" + info.getString("channelId") + ")", true);
        embed.addField("Duration", durationParser(length), true);
        // Put view count if there is one
        if(stats.has("viewCount"))
            embed.addField("Views", NumberFormat.getNumberInstance(Locale.US).format(views), true);
        else
            embed.addField("Views", "Unknown", true);
        // Add and format rating
        embed.addField("Rating", "<:ytup:717600455580188683> **" + NumberFormat.getNumberInstance(Locale.US).format(likes) + "** *(" + percent + "%)*\n" +
                        "<:ytdown:717600455353696317> **" + NumberFormat.getNumberInstance(Locale.US).format(dislike) + "** *(" + dispercent + "%)*", true);
        embed.addField("Uploaded", dateParser(upload), true);
        embed.setThumbnail(url.getJSONArray("items").getJSONObject(0).getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("maxres").getString("url"));
        embed.setColor(Color.decode("#FF0001"));
        return embed;
    }

    /**
     * Parse the duration as it appears on Youtube dot com
     * @param duration the duration provided from the api
     * @return a parsed duration
     */
    public String durationParser(String duration) {
        duration = duration.replace("PT", "");
        String[] chars = duration.split("");
        StringBuilder output = new StringBuilder();
        for(int i = 0; i < duration.length() - 1; i++) {
            String chari = chars[i];
            try {
                Integer.parseInt(chari);
                output.append(chari);
            } catch (NumberFormatException e) {
                output.append(":");
            }
        }
        return output.toString();
    }

    /**
     * Parse the date because java is weird
     * @param date the date from the api
     * @return the parsed date
     */
    public String dateParser(String date) {
        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssX");
        OffsetDateTime odtInstanceAtOffset = OffsetDateTime.parse(date, inputFormat);
        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("MM/dd/uuuu'\n'HH:mm' UTC'");
        return odtInstanceAtOffset.format(outputFormat);
    }
}
