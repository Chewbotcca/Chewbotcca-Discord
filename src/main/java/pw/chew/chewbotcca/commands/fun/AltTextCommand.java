/*
 * Copyright (C) 2022 Chewbotcca
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

package pw.chew.chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.CooldownScope;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.MiscUtil;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.RestClient;

import java.util.Collections;

public class AltTextCommand extends SlashCommand {

    public AltTextCommand() {
        this.name = "alt";
        this.help = "Uses Azure AI to generate a caption for this image";
        this.cooldown = 60;
        this.cooldownScope = CooldownScope.USER;
        this.options = Collections.singletonList(
            new OptionData(OptionType.ATTACHMENT, "image", "The image you want alt text generated for. Up to 4 MB.", true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String imageUrl = event.optAttachment("image", null).getUrl();

        Request request = new Request.Builder()
            .url(PropertiesManager.getAzureInstanceURL() + "/vision/v1.0/describe?maxCandidates=5&language=en")
            .post(RequestBody.create(new JSONObject().put("url", imageUrl).toString(), RestClient.JSON))
            .addHeader("User-Agent", RestClient.userAgent)
            .addHeader("Content-Type", "application/json")
            .addHeader("Ocp-Apim-Subscription-Key", PropertiesManager.getAzureKey())
            .build();

        JSONObject response = new JSONObject(RestClient.performRequest(request));
        if (response.has("code")) {
            event.reply(response.getString("message")).setEphemeral(true).queue();
            return;
        }

        JSONObject description = response.getJSONObject("description");

        String captions = String.join("\n", MiscUtil.toList(description.getJSONArray("captions"), JSONObject.class).stream()
            .map(data -> "\"" + data.getString("text") + "\" (" + MiscUtil.formatPercent(data.getFloat("confidence")) + ")")
            .toList());

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("Alt Text Result")
            .setDescription(captions)
            .addField("Tags", String.join(", ", MiscUtil.toList(description.getJSONArray("tags"), String.class)), true)
            .setImage(imageUrl);

        event.replyEmbeds(embed.build()).queue();
    }
}
