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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class YouTubeCommand extends Command {
    static ArrayList<String> describedIds = new ArrayList<>();

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

    public EmbedBuilder response(JSONObject url, String id) {
        JSONObject stats = url.getJSONArray("items").getJSONObject(0).getJSONObject("statistics");
        JSONObject info = url.getJSONArray("items").getJSONObject(0).getJSONObject("snippet");
        String length = url.getJSONArray("items").getJSONObject(0).getJSONObject("contentDetails").getString("duration");
        int views = Integer.parseInt(stats.getString("viewCount"));
        int likes = Integer.parseInt(stats.getString("likeCount"));
        int dislike = Integer.parseInt(stats.getString("dislikeCount"));
        String upload = info.getString("publishedAt");
        DecimalFormat df = new DecimalFormat("#.##");
        float totallikes = likes + dislike;
        String percent = df.format(likes / totallikes * 100);
        String dispercent = df.format(dislike / totallikes * 100);

        String urlpls = "http://youtu.be/" + id;

        return new EmbedBuilder()
                .setAuthor("YouTube Video Search")
                .setTitle(info.getString("title"), urlpls)
                .addField("Uploader", "[" + info.getString("channelTitle") + "](https://youtube.com/channel/" + info.getString("channelId") + ")", true)
                .addField("Duration", durationParser(length), true)
                .addField("Views", NumberFormat.getNumberInstance(Locale.US).format(views), true)
                .addField("Rating", "<:ytup:717600455580188683> **" + NumberFormat.getNumberInstance(Locale.US).format(likes) +  "** *(" + percent + "%)*\n" +
                        "<:ytdown:717600455353696317> **" + NumberFormat.getNumberInstance(Locale.US).format(dislike) + "** *(" + dispercent + "%)*", true)
                .addField("Uploaded", dateParser(upload), true)
                .setColor(Color.decode("#FF0001"));
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
