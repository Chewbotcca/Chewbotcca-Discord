package pw.chew.chewbotcca.commands.google;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.RestClient;

import java.net.MalformedURLException;
import java.net.URL;

public class OCRCommand extends Command {

    public OCRCommand() {
        this.name = "ocr";
        this.cooldown = 60;
        this.cooldownScope = CooldownScope.USER;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isBlank() && event.getMessage().getAttachments().isEmpty()) {
            event.reply("Please specify a image URL or upload an image!");
            return;
        }

        String url;
        if (event.getMessage().getAttachments().isEmpty()) {
            try {
              url = new URL(event.getArgs()).toString();
            } catch (MalformedURLException e) {
                event.reply("The provided arguments is not a valid URL!");
                return;
            }
        } else {
            url = event.getMessage().getAttachments().get(0).getUrl();
        }
        event.getChannel().sendTyping().queue();

        JSONObject body = new JSONObject("{\"requests\":[{\"image\":{\"source\":{\"imageUri\": \"" + url + "\"}},\"features\":[{\"type\":\"TEXT_DETECTION\"}]}]}");

        JSONObject response = new JSONObject(
            RestClient.post("https://vision.googleapis.com/v1/images:annotate?key=" + PropertiesManager.getGoogleKey(), body)
        );

        try {
            JSONObject data = response.getJSONArray("responses").getJSONObject(0);
            if (data.isEmpty()) {
                event.reply("No text found, sorry :(");
                return;
            }
            String text = data.getJSONObject("fullTextAnnotation").getString("text")
                .replaceAll("\r", "")
                .replaceAll("\n\n", "\n");
            if (text.length() > 2048) {
                text = text.substring(0, 2044) + "...";
            }
            event.reply(new EmbedBuilder()
                .setTitle("OCR Results")
                .setDescription(text)
                .build());
        } catch (JSONException e) {
            event.reply("JSON Exception Occurred, sorry :(");
        }
    }
}
