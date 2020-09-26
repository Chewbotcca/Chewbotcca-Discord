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
        if (!file.contains("cdn.discordapp.com")) {
            event.reply("Only cdn.discordapp.com urls are supported at this time!");
            return;
        }
        if (pasted.containsKey(file)) {
            event.reply("Already pasted this! Link: " + pasted.get(file));
            return;
        }

        event.getChannel().sendTyping().queue();

        String contents = RestClient.get(file);
        String name = file.split("/")[file.split("/").length - 1];

        JSONObject payload = new JSONObject()
            .put("description", "Uploaded file from Chewbotcca Discord Bot")
            .put("sections", new JSONArray().put(new JSONObject()
                .put("name", name)
                .put("syntax", "autodetect")
                .put("contents", contents)
            ));

        JSONObject response = new JSONObject(RestClient.post("https://api.paste.ee/v1/pastes?key=" + PropertiesManager.getPasteEEKey(), payload));

        if (response.has("link")) {
            event.reply("Your paste for" + name + " is available at: " + response.getString("link"));
            pasted.put(file, response.getString("link"));
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
            if (attachment.getSize() > 12000000)
                continue;

            return attachment.getUrl();
        }
        return null;
    }
}
