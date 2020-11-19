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
package pw.chew.chewbotcca.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONObject;
import pw.chew.chewbotcca.objects.Profile;
import pw.chew.chewbotcca.util.DateTime;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.RestClient;

import java.awt.Color;

// %^lastfm command
public class LastFMCommand extends Command {

    public LastFMCommand() {
        this.name = "lastfm";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        // Make sure last.fm token exists
        // not sure why i only do this here
        String key = PropertiesManager.getLastfmToken();
        if (key == null) {
            event.reply("This command requires an API key from last.fm!");
            return;
        }

        // Get args, assume it's a username, and find their stats
        String args = event.getArgs();
        if(args.length() == 0) {
            Profile profile = Profile.getProfile(event.getAuthor().getId());
            if (profile.getLastFm() != null) {
                args = profile.getLastFm();
            } else {
                event.reply("You don't have a last.fm username set on your profile. Please specify a user with `" + event.getPrefix() + "lastfm user` or set your username with `" + event.getPrefix() + "profile set lastfm yourname`!");
                return;
            }
        }
        JSONObject parse = new JSONObject(RestClient.get("http://ws.audioscrobbler.com/2.0/?method=user.getrecenttracks&limit=1&user=" + args + "&api_key=" + key + "&format=json"));
        // But if I got bamboozled
        if(parse.has("message") && parse.getString("message").equals("User not found")) {
            event.reply("No user found for the provided input!");
            return;
        }

        // Get user recent tracks and its info
        JSONObject base = parse.getJSONObject("recenttracks").getJSONArray("track").getJSONObject(0);
        String user = parse.getJSONObject("recenttracks").getJSONObject("@attr").getString("user");
        String artist = base.getJSONObject("artist").getString("#text");
        String track = base.getString("name");
        String album = base.getJSONObject("album").getString("#text");

        // if now playing
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

        // Generate and send info
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


