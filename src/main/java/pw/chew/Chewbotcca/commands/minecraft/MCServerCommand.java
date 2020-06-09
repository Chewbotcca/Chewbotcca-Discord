package pw.chew.Chewbotcca.commands.minecraft;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONObject;
import pw.chew.Chewbotcca.util.RestClient;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class MCServerCommand extends Command {

    public MCServerCommand() {
        this.name = "mcserver";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.getChannel().sendTyping().queue();
        JSONObject data = new JSONObject(RestClient.get("https://eu.mc-api.net/v3/server/ping/" + commandEvent.getArgs()));
        EmbedBuilder e = new EmbedBuilder();
        e.setTitle("**Server Info For** `" + commandEvent.getArgs() + "`");
        if(data.has("error")) {
            e.setColor(Color.decode("#ff0000"));
            e.addField("Error", data.getString("error"), true);
            commandEvent.reply(e.build());
            return;
        }
        e.setThumbnail(data.getString("favicon"));

        String online;
        if (data.getBoolean("online")) {
            online = "Online";
            e.setColor(Color.decode("#00ff00"));
        } else {
            online = "Offline";
            e.setColor(Color.decode("#FF0000"));
        }

        e.addField("Status", online, true);
        e.addField("Players", data.getJSONObject("players").getInt("online") + "/" + data.getJSONObject("players").getInt("max"), true);
        e.addField("Version", data.getJSONObject("version").getString("name"), true);

        String fetched = data.getString("fetch");
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX");
        OffsetDateTime odtInstanceAtOffset = OffsetDateTime.parse(fetched, DATE_TIME_FORMATTER);
        e.setFooter("Last fetched");
        e.setTimestamp(odtInstanceAtOffset);

        commandEvent.reply(e.build());
    }
}
