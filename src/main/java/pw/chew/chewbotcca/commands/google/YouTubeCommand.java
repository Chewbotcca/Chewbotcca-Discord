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
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.RestClient;

import java.awt.Color;
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
        this.aliases = new String[]{"yt"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.cooldown = 5;
        this.cooldownScope = CooldownScope.USER;
    }

    @Override
    protected void execute(CommandEvent event) {
        // Get the input and find results
        String search = event.getArgs();
        String id = null;
        if (search.contains("youtube.com")) {
            id = search.split("=")[1];
        } else if (search.contains("youtu.be")) {
            id = search.split("/")[search.split("/").length - 1];
        }
        String findidurl = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&q=" + URLEncoder.encode(search, StandardCharsets.UTF_8) + "&key=" + PropertiesManager.getGoogleKey();
        // Find a video if there is one
        if (id == null) {
            try {
                id = new JSONObject(RestClient.get(findidurl)).getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId");
            } catch (JSONException e) {
                event.reply("No videos found!");
                return;
            }
        }
        // Get the video
        JSONObject url = new JSONObject(RestClient.get("https://www.googleapis.com/youtube/v3/videos?id=" + id + "&key=" + PropertiesManager.getGoogleKey() + "&part=snippet,contentDetails,statistics"));
        if (url.getJSONObject("pageInfo").getInt("totalResults") == 0) {
            event.reply("No results found.");
            return;
        }

        event.reply(response(url, id, event.getChannel()).build());
    }

    /**
     * Generate an embed for the specified video
     * @param url the json object from youtube
     * @param id the video id
     * @return an embedbuilder ready to be built
     */
    public EmbedBuilder response(JSONObject url, String id, MessageChannel channel) {
        boolean restricted = url
            .getJSONArray("items")
            .getJSONObject(0)
            .getJSONObject("contentDetails")
            .getJSONObject("contentRating")
            .has("ytRating");

        // Don't respond if not nsfw channel
        if(restricted && channel.getType() == ChannelType.TEXT && !((TextChannel)channel).isNSFW()) {
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
        if(stats.has("viewCount")) {
            views = Long.parseLong(stats.getString("viewCount"));
        }
        int likes = -1, dislike = -1;
        float totalLikes;
        String percent = "", disPercent = "";
        DecimalFormat df = new DecimalFormat("#.##");
        if(stats.has("likeCount")) {
            likes = Integer.parseInt(stats.getString("likeCount"));
            dislike = Integer.parseInt(stats.getString("dislikeCount"));
            totalLikes = likes + dislike;
            percent = df.format(likes / totalLikes * 100);
            disPercent = df.format(dislike / totalLikes * 100);
        }

        String urlpls = "http://youtu.be/" + id;

        // Finally make the embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("YouTube Video Search");
        embed.setTitle(info.getString("title"), urlpls);
        embed.addField("Uploader", "[" + info.getString("channelTitle") + "](https://youtube.com/channel/" + info.getString("channelId") + ")", true);
        embed.addField("Duration", durationParser(length), true);
        // Put view count if there is one
        if(stats.has("viewCount")) {
            embed.addField("Views", NumberFormat.getNumberInstance(Locale.US).format(views), true);
        } else {
            embed.addField("Views", "Unknown", true);
        }
        // Add and format rating
        if (likes > -1) {
            embed.addField("Rating", "<:ytup:717600455580188683> **" + NumberFormat.getNumberInstance(Locale.US).format(likes) + "** *(" + percent + "%)*\n" +
                "<:ytdown:717600455353696317> **" + NumberFormat.getNumberInstance(Locale.US).format(dislike) + "** *(" + disPercent + "%)*", true);
        }
        embed.setFooter("Uploaded");
        embed.setTimestamp(dateParser(info.getString("publishedAt")));
        embed.setThumbnail(getBestThumbnail(url.getJSONArray("items").getJSONObject(0).getJSONObject("snippet").getJSONObject("thumbnails")));
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
                if (chari.length() < 2 && output.toString().contains(":")) {
                    chari = "0" + chari;
                }
                output.append(chari);
            } catch (NumberFormatException e) {
                output.append(":");
            }
        }
        String response = output.toString();
        if (!response.contains(":")) {
            if (response.length() < 2) {
                response = "0:0" + response;
            } else {
                response = "0:" + response;
            }
        }
        return response;
    }

    /**
     * Finds the best thumbnail given all the choices
     * @param thumbnails the thumbnail object
     * @return the thumbnail
     */
    public String getBestThumbnail(JSONObject thumbnails) {
        // If no best thumbnail, just use YouTube logo
        String bestThumbnail = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a0/YouTube_social_red_circle_(2017).svg/1200px-YouTube_social_red_circle_(2017).svg.png";
        int currentWidth = 0;
        int currentHeight = 0;
        for (String value : thumbnails.keySet()) {
            JSONObject thumbnail = thumbnails.getJSONObject(value);
            if (thumbnail.getInt("width") > currentWidth && thumbnail.getInt("height") > currentHeight) {
                bestThumbnail = thumbnail.getString("url");
                currentWidth = thumbnail.getInt("width");
                currentHeight = thumbnail.getInt("height");
            }
        }
        return bestThumbnail;
    }

    /**
     * Parse the date because java is weird
     * @param date the date from the api
     * @return the parsed date
     */
    public OffsetDateTime dateParser(String date) {
        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssX");
        return OffsetDateTime.parse(date, inputFormat);
    }
}
