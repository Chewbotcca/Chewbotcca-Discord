/*
 * Copyright (C) 2022 Chewbotcca
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
package pw.chew.chewbotcca.commands.services.google;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.CooldownScope;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.chewbotcca.objects.services.YouTubeVideo;
import pw.chew.chewbotcca.util.DateTime;
import pw.chew.chewbotcca.util.MiscUtil;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.ResponseHelper;
import pw.chew.chewbotcca.util.RestClient;

import java.awt.Color;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Collections;

public class YouTubeCommand extends SlashCommand {

    public YouTubeCommand() {
        this.name = "youtube";
        this.help = "Queries YouTube for a video";
        this.aliases = new String[]{"yt"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.cooldown = 5;
        this.cooldownScope = CooldownScope.USER;
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "query", "The query to search for videos with.", true)
        );
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String query = event.getArgs();
            TextChannel channel = event.getTextChannel();

            YouTubeVideo video = getVideo(query);

            // Don't respond if not nsfw channel
            if (video.isRestricted() && channel.getType() == ChannelType.TEXT && !channel.isNSFW()) {
                throw new IllegalArgumentException("The returned video is marked as age restricted by YouTube, and may only be shown in NSFW channels or DMs. Sorry!");
            }

            event.reply(buildVideoEmbed(video).build());
        } catch (IllegalArgumentException e) {
            event.replyError("Error occurred! " + e.getMessage());
        }
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        try {
            String query = event.optString("query", "");

            YouTubeVideo video = getVideo(query);

            // Don't respond if not nsfw channel
            if (video.isRestricted() && event.getChannel().getType() == ChannelType.TEXT && !event.getTextChannel().isNSFW()) {
                throw new IllegalArgumentException("The returned video is marked as age restricted by YouTube, and may only be shown in NSFW channels or DMs. Sorry!");
            }

            event.replyEmbeds(buildVideoEmbed(video).build()).queue();
        } catch (IllegalArgumentException e) {
            event.replyEmbeds(ResponseHelper.generateFailureEmbed("Error occurred!", e.getMessage())).queue();
        }
    }

    /**
     * Gathers info for our commands to handle
     *
     * @param search the query
     * @throws IllegalArgumentException If no videos could be found
     * @return a response
     */
    public static YouTubeVideo getVideo(String search) {
        // Get the input and find results
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
                throw new IllegalArgumentException("No videos found for query!");
            }
        }
        // Get the video
        JSONObject url = new JSONObject(RestClient.get("https://www.googleapis.com/youtube/v3/videos?id=" + id + "&key=" + PropertiesManager.getGoogleKey() + "&part=snippet,contentDetails,statistics"));
        if (url.getJSONObject("pageInfo").getInt("totalResults") == 0) {
            throw new IllegalArgumentException("No results found for query!");
        }

        return new YouTubeVideo(url.getJSONArray("items").getJSONObject(0));
    }

    /**
     * Generate an embed for the specified video
     *
     * @param video a youtube video object
     * @return an embedbuilder ready to be built
     */
    public static EmbedBuilder buildVideoEmbed(YouTubeVideo video) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("YouTube Video Search");
        embed.setTitle(video.getTitle(), video.getURL());
        embed.addField("Uploader", String.format("[%s](%s)", video.getUploaderName(), video.getUploaderURL()), true);
        embed.addField("Duration", video.getDuration(), true);
        // Put view count if there is one
        embed.addField("Views", video.getViews() == null ? "Unknown" : video.getViews(), true);

        // Add and format rating
        if (video.getLikes() != null || video.getDislikes() != null) {
            String likes, dislikes;
            if (video.getLikes() == null || video.getDislikes() == null) {
                likes = video.getLikes() == null ? "<:ytup:717600455580188683> Unknown" : String.format("<:ytup:717600455580188683> **%s**", MiscUtil.delimitNumber(video.getLikes()));
                dislikes = video.getDislikes() == null ? "<:ytdown:717600455353696317> Unknown" : String.format("<:ytdown:717600455353696317> **%s**", MiscUtil.delimitNumber(video.getDislikes()));
            } else {
                float totalLikes = video.getLikes() + video.getDislikes();
                likes = String.format("<:ytup:717600455580188683> **%s** *(%s)*", MiscUtil.delimitNumber(video.getLikes()), MiscUtil.formatPercent(video.getLikes() / totalLikes));
                dislikes = String.format("<:ytdown:717600455353696317> **%s** *(%s)*", MiscUtil.delimitNumber(video.getDislikes()), MiscUtil.formatPercent(video.getDislikes() / totalLikes));
            }
            embed.addField("Rating", likes + "\n" + dislikes, true);
        }

        OffsetDateTime uploaded = video.getUploadDate();
        embed.addField("Uploaded", TimeFormat.DATE_TIME_LONG.format(uploaded) + "\n" + DateTime.timeAgoShort(uploaded.toInstant(), true) + " ago", true);
        embed.setFooter("Video ID: " + video.getId());
        embed.setThumbnail(getBestThumbnail(video.getThumbnails()));
        embed.setColor(Color.RED);
        return embed;
    }

    /**
     * Finds the best thumbnail given all the choices
     *
     * @param thumbnails the thumbnail object
     * @return the thumbnail
     */
    private static String getBestThumbnail(JSONObject thumbnails) {
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
}
