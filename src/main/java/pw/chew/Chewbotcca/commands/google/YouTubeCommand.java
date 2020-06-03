package pw.chew.Chewbotcca.commands.google;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONObject;
import pw.chew.Chewbotcca.Main;
import pw.chew.Chewbotcca.util.RestClient;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class YouTubeCommand extends Command {

    public YouTubeCommand() {
        this.name = "youtube";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        String search = event.getArgs();
        String findidurl = null;
        try {
            findidurl = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&q=" + URLEncoder.encode(search, "UTF-8") + "&key=" + Main.getProp().getProperty("google");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        String id = new JSONObject(RestClient.get(findidurl)).getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId");
        JSONObject url = new JSONObject(RestClient.get("https://www.googleapis.com/youtube/v3/videos?id=" + id + "&key=" + Main.getProp().getProperty("google") + "&part=snippet,contentDetails,statistics"));
        if (url.getJSONObject("pageInfo").getInt("totalResults") == 0) {
            event.reply("No results found.");
            return;
        }
        event.reply(response(url, id).build());
    }

    public String monthToString(String input) {
        switch (input) {
            case "01":
                return "January";
            case "02":
                return "February";
            case "03":
               return "March";
            case "04":
                return "April";
            case "05":
                return "May";
            case "06":
                return "June";
            case "07":
                return "July";
            case "08":
                return "August";
            case "09":
                return "September";
            case "10":
                return "October";
            case "11":
                return "November";
            case "12":
                return "December";
        }
        return "what";
    }

    public EmbedBuilder response(JSONObject url, String id) {
        JSONObject stats = url.getJSONArray("items").getJSONObject(0).getJSONObject("statistics");
        JSONObject info = url.getJSONArray("items").getJSONObject(0).getJSONObject("snippet");
        String length = url.getJSONArray("items").getJSONObject(0).getJSONObject("contentDetails").getString("duration");
        String views = stats.getString("viewCount");
        String likes = stats.getString("likeCount");
        String dislike = stats.getString("dislikeCount");
        String upload = info.getString("publishedAt");
        /*
        upload = info["publishedAt"][0. .9]
        upload = upload.split("-")
        */
        float totallikes = Integer.parseInt(likes) + Integer.parseInt(dislike);
        float percent = (Integer.parseInt(likes) / totallikes * 100);
        float dispercent = (Integer.parseInt(dislike) / totallikes * 100);

        String urlpls = "http://youtu.be/" + id;

        return new EmbedBuilder()
                .setTitle("YouTube Video Search")
                .addField("Title", info.getString("title"), true)
                .addField("Uploader", info.getString("channelTitle"), true)
                .addField("Duration", length, true)
                .addField("Views", views, true)
                .addField("Rating", "<:ytup:469274644982267905> **" + likes +  "** *(" + percent + "%)*\n" +
                        "<:ytdown:469274880416940042> **" + dislike + "** *(" + dispercent + "%)*", true)
                .addField("Uploaded", upload, true)
                .addField("Video URL", urlpls, true)
                .setColor(Color.decode("#FF0001"));
    }
}
