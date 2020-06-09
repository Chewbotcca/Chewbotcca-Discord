package pw.chew.Chewbotcca.commands.minecraft;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.Chewbotcca.util.RestClient;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class MCUserCommand extends Command {

    public MCUserCommand() {
        this.name = "mcuser";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        String name = commandEvent.getArgs().split(" ")[0].replace("-", "");
        JSONArray history;
        String uuid;
        if(name.length() == 32) {
            try {
                history = new JSONArray(RestClient.get("https://api.mojang.com/user/profiles/" + name + "/names"));
                uuid = name;
            } catch (JSONException e) {
                commandEvent.reply("Not a valid input! Please enter a valid UUID!");
                return;
            }
        } else if (name.length() >= 3 && name.length() <= 16) {
            try {
                JSONObject profile = new JSONObject(RestClient.get("https://api.mojang.com/users/profiles/minecraft/" + name));
                history = new JSONArray(RestClient.get("https://api.mojang.com/user/profiles/" + profile.getString("id") + "/names"));
                uuid = profile.getString("id");
            } catch (JSONException e) {
                commandEvent.reply("Not a valid input! Please enter a valid UUID!");
                return;
            }
        } else {
            commandEvent.reply("Not a valid input! Please enter a valid username or a valid UUID!");
            return;
        }
        StringBuilder names = new StringBuilder();
        for(int i = history.length() - 1; i >= 0; i--) {
            JSONObject data = history.getJSONObject(i);
            String time;
            if(data.has("changedToAt")) {
                OffsetDateTime at = Instant.ofEpochMilli(data.getLong("changedToAt")).atOffset(ZoneOffset.UTC);
                DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/uuuu' @ 'HH:mm' UTC'");
                time = at.format(DATE_TIME_FORMATTER);
            } else {
                time = "Original";
            }
            String username = data.getString("name");
            names.append(time).append(" - ").append(username).append("\n");
        }
        commandEvent.reply(new EmbedBuilder()
                .setTitle("Minecraft Profile Information for " + history.getJSONObject(history.length() - 1).getString("name"))
                .setDescription("[NameMC Profile](https://namemc.com/profile/" + uuid + ")")
                .setThumbnail("https://minotar.net/helm/" + uuid)
                .addField("Name History", names.toString(), true)
        .build());
    }
}