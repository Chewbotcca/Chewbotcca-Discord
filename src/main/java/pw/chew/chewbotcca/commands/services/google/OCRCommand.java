/*
 * Copyright (C) 2024 Chewbotcca
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
package pw.chew.chewbotcca.commands.services.google;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.CooldownScope;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.RestClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

public class OCRCommand extends SlashCommand {

    public OCRCommand() {
        this.name = "ocr";
        this.help = "Finds text on images, turns out iOS 15 does this now but whatever";
        this.cooldown = 60;
        this.cooldownScope = CooldownScope.USER;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "url", "The image URL you want to OCR").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        try {
            String url = new URL(event.optString("url", "")).toString();
            event.replyEmbeds(gatherData(url)).queue();
        } catch (MalformedURLException e) {
            event.reply("The provided arguments is not a valid URL!").setEphemeral(true).queue();
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
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

        try {
            event.reply(gatherData(url));
        } catch (IllegalArgumentException e) {
            event.replyWarning(e.getMessage());
        }
    }

    private MessageEmbed gatherData(String url) {
        JSONObject body = new JSONObject("{\"requests\":[{\"image\":{\"source\":{\"imageUri\": \"" + url + "\"}},\"features\":[{\"type\":\"TEXT_DETECTION\"}]}]}");

        JSONObject response = RestClient.post("https://vision.googleapis.com/v1/images:annotate?key=" + PropertiesManager.getGoogleKey(), body).asJSONObject();

        try {
            JSONObject data = response.getJSONArray("responses").getJSONObject(0);
            if (data.isEmpty()) {
                throw new IllegalArgumentException("No text found, sorry :(");
            }
            String text = data.getJSONObject("fullTextAnnotation").getString("text")
                .replaceAll("\r", "")
                .replaceAll("\n\n", "\n");
            if (text.length() > 2048) {
                text = text.substring(0, 2044) + "...";
            }
            return new EmbedBuilder()
                .setTitle("OCR Results")
                .setDescription(text)
                .build();
        } catch (JSONException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("JSON Exception Occurred, sorry :(");
        }
    }
}
