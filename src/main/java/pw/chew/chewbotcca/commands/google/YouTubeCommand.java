package pw.chew.chewbotcca.commands.google;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
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
import java.util.ArrayList;
import java.util.Locale;

public class YouTubeCommand extends Command {
    final static ArrayList<String> describedIds = new ArrayList<>();

    public YouTubeCommand() {
        this.name = "youtube";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.cooldown = 5;
        this.cooldownScope = CooldownScope.USER;
    }

    @Override
    protected void execute(CommandEvent event) {
        String search = event.getArgs();
        String findidurl = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&q=" + URLEncoder.encode(search, StandardCharsets.UTF_8) + "&key=" + PropertiesManager.getGoogleKey();
        String id;
        try {
            id = new JSONObject(RestClient.get(findidurl)).getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId");
        } catch (JSONException e) {
            event.reply("No videos found!");
            return;
        }
        JSONObject url = new JSONObject(RestClient.get("https://www.googleapis.com/youtube/v3/videos?id=" + id + "&key=" + PropertiesManager.getGoogleKey() + "&part=snippet,contentDetails,statistics"));
        if (url.getJSONObject("pageInfo").getInt("totalResults") == 0) {
            event.reply("No results found.");
            return;
        }
        event.reply(response(url, id).build());
    }

    public EmbedBuilder response(JSONObject url, String id) {
        JSONObject stats = url.getJSONArray("items").getJSONObject(0).getJSONObject("statistics");
        JSONObject info = url.getJSONArray("items").getJSONObject(0).getJSONObject("snippet");
        String length = url.getJSONArray("items").getJSONObject(0).getJSONObject("contentDetails").getString("duration");
        long views = 0;
        if(stats.has("viewCount"))
            views = Long.parseLong(stats.getString("viewCount"));
        int likes = Integer.parseInt(stats.getString("likeCount"));
        int dislike = Integer.parseInt(stats.getString("dislikeCount"));
        String upload = info.getString("publishedAt");
        DecimalFormat df = new DecimalFormat("#.##");
        float totallikes = likes + dislike;
        String percent = df.format(likes / totallikes * 100);
        String dispercent = df.format(dislike / totallikes * 100);

        String urlpls = "http://youtu.be/" + id;

        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("YouTube Video Search");
        embed.setTitle(info.getString("title"), urlpls);
        embed.addField("Uploader", "[" + info.getString("channelTitle") + "](https://youtube.com/channel/" + info.getString("channelId") + ")", true);
        embed.addField("Duration", durationParser(length), true);
        if(stats.has("viewCount"))
            embed.addField("Views", NumberFormat.getNumberInstance(Locale.US).format(views), true);
        else
            embed.addField("Views", "Unknown", true);
        embed.addField("Rating", "<:ytup:717600455580188683> **" + NumberFormat.getNumberInstance(Locale.US).format(likes) + "** *(" + percent + "%)*\n" +
                        "<:ytdown:717600455353696317> **" + NumberFormat.getNumberInstance(Locale.US).format(dislike) + "** *(" + dispercent + "%)*", true);
        embed.addField("Uploaded", dateParser(upload), true);
        embed.setThumbnail(url.getJSONArray("items").getJSONObject(0).getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("maxres").getString("url"));
        embed.setColor(Color.decode("#FF0001"));
        return embed;
    }

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

    public String dateParser(String date) {
        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssX");
        OffsetDateTime odtInstanceAtOffset = OffsetDateTime.parse(date, inputFormat);
        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("MM/dd/uuuu'\n'HH:mm' UTC'");
        return odtInstanceAtOffset.format(outputFormat);
    }

    public static boolean didDescribe(String id) {
        return describedIds.contains(id);
    }

    public static void described(String id) {
        describedIds.add(id);
    }
}
