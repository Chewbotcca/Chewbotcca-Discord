package pw.chew.Chewbotcca.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.Chewbotcca.util.RestClient;

import java.awt.*;

public class MixerCommand extends Command {

    public MixerCommand() {
        this.name = "mixer";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        JSONObject parse;
        try {
            parse = new JSONObject(RestClient.get("https://mixer.com/api/v1/channels/" + event.getArgs()));
        } catch (JSONException e) {
            event.reply("User not found!");
            return;
        }
        String name = parse.getString("token");
        String online;
        if (parse.getBoolean("online")) {
           online = "Currently Streaming!";
        }  else{
            online = "Currently Offline";
        }
        int followers = parse.getInt("numFollowers");
        String avatar = parse.getJSONObject("user").getString("avatarUrl");

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Mixer info for user " + name, "http://mixer.com/" + name)
                .setDescription(online)
                .setThumbnail(avatar)
                .addField("Stream Title", parse.getString("name"), false)
                .addField("Followers", String.valueOf(followers), true)
                .addField("Total Views", String.valueOf(parse.getInt("viewersTotal")), true);
        if (parse.getBoolean("online")) {
            embed.setColor(Color.decode("#43B581"));
        } else {
            embed.setColor(Color.decode("#FAA61A"));
        }

        event.reply(embed.build());
    }
}
