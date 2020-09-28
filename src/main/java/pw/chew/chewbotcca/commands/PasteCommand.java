package pw.chew.chewbotcca.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PasteCommand extends Command {
    Map<String, String> pasted = new HashMap<>();

    public PasteCommand() {
        this.name = "paste";
        this.cooldown = 15;
        this.cooldownScope = CooldownScope.USER;
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        String file;
        if (event.getArgs().isBlank() && event.getChannelType() == ChannelType.TEXT) {
            file = getRecentUpload(event.getTextChannel());
        } else {
            file = event.getArgs();
        }
        if (file == null) {
            event.reply("Please link to the file you want to embed!");
            return;
        }

        String name = file.split("/")[file.split("/").length - 1];

        if (!file.contains("cdn.discordapp.com")) {
            event.reply("Only cdn.discordapp.com urls are supported at this time!");
            return;
        }
        if (pasted.containsKey(file)) {
            event.reply("Already pasted " + name + "! Link: https://paste.gg/chewbotcca/" + pasted.get(file));
            return;
        }

        event.getChannel().sendTyping().queue();

        String contents = RestClient.get(file);

        JSONObject payload = new JSONObject()
            .put("name", "Uploaded file from Chewbotcca Discord Bot")
            .put("files", new JSONArray().put(new JSONObject()
                .put("name", name)
                .put("content", new JSONObject()
                    .put("format", "text")
                    .put("highlight_language", "null")
                    .put("value", contents)
                )
            ));

        JSONObject response = new JSONObject(RestClient.post("https://api.paste.gg/v1/pastes", "Key " + PropertiesManager.getPasteGgKey(), payload));

        if (response.getString("status").equals("success")) {
            event.reply("Your paste for " + name + " is available at: https://paste.gg/chewbotcca/" + response.getJSONObject("result").getString("id"));
            pasted.put(file, response.getJSONObject("result").getString("id"));
        }
    }

    public String getRecentUpload(TextChannel channel) {
        List<Message> messages = channel.getHistory().retrievePast(10).complete();
        for (Message message : messages) {
            if (message.getAttachments().isEmpty())
                continue;

            Message.Attachment attachment = message.getAttachments().get(0);
            if (attachment.isImage() || attachment.isVideo())
                continue;

            return attachment.getUrl();
        }
        return null;
    }
}
