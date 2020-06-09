package pw.chew.Chewbotcca.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONObject;
import pw.chew.Chewbotcca.Main;
import pw.chew.Chewbotcca.util.DateTime;
import pw.chew.Chewbotcca.util.RestClient;

import java.awt.*;
import java.util.Properties;

public class LastFMCommand extends Command {

    public LastFMCommand() {
        this.name = "lastfm";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        Properties prop = Main.getProp();
        String key = prop.getProperty("lastfm");
        if (key == null) {
            event.reply("This command requires an API key from last.fm!");
            return;
        }

        String args = event.getArgs();

        JSONObject parse = new JSONObject(RestClient.get("http://ws.audioscrobbler.com/2.0/?method=user.getrecenttracks&limit=1&user=" + args + "&api_key=" + key + "&format=json"));

        if(parse.has("message") && parse.getString("message").equals("User not found")) {
            event.reply("No user found for the provided input!");
            return;
        }

        JSONObject base = parse.getJSONObject("recenttracks").getJSONArray("track").getJSONObject(0);

        String user = parse.getJSONObject("recenttracks").getJSONObject("@attr").getString("user");

        String artist = base.getJSONObject("artist").getString("#text");
        String track = base.getString("name");
        String album = base.getJSONObject("album").getString("#text");

        boolean playing;
        String timeago = null;
        if (base.has("@attr")) {
            playing = true;
        } else {
            long np = Integer.parseInt(base.getJSONObject("date").getString("uts"));
            long t = ((System.currentTimeMillis()) - (np * 1000));
            timeago = DateTime.timeAgo(t);
            playing = false;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Last.fm status for " + user)
                .addField("Track", track, true)
                .addField("Artist", artist, true)
                .addField("Album", album, true);

        if (playing) {
            embed.setColor(Color.decode("#00FF00"));
            embed.setDescription("Currently listening!");
        } else {
            embed.setColor(Color.decode("#FF0000"));
            embed.setDescription("Last listened about " + timeago + " ago.");
        }

        event.reply(embed.build());
    }
}


