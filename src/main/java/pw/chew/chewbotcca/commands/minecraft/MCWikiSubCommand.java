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
package pw.chew.chewbotcca.commands.minecraft;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.json.JSONArray;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import pw.chew.chewbotcca.util.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// %^mcwiki command
public class MCWikiSubCommand extends SlashCommand {
    public MCWikiSubCommand() {
        this.name = "wiki";
        this.help = "Search the Minecraft Wiki";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "query", "The query to lookup on the wiki").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        try {
            event.replyEmbeds(gatherData(event.optString("query", ""))).queue();
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    private MessageEmbed gatherData(String query) {
        String apiUrl = "https://minecraft.wiki/api.php?action=opensearch&search=";
        String mcUrl = "https://minecraft.wiki/";

        JSONArray j;

        // Try and find a result
        try {
            j = RestClient.get(apiUrl + URLEncoder.encode(query, StandardCharsets.UTF_8)).asJSONArray().getJSONArray(1);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Error reading search results!");
        }
        if(j.isEmpty()) {
            throw new IllegalArgumentException("No results found!");
        }

        // If there's a result, find the right article
        String articleName;
        List<String> noSlash = new ArrayList<>();
        for(Object item : j) {
            String str = (String)item;
            String name = str.replace("https://minecraft.wiki/", "");
            if(!name.contains("/")) {
                noSlash.add((String) item);
            }
        }

        if(!noSlash.isEmpty())
            articleName = noSlash.get(0).replaceAll(" ", "_");
        else
            articleName = j.getString(0).replaceAll(" ", "_");

        String url = mcUrl + articleName.replaceAll(" ", "_");

        // Actually get the page
        String page = RestClient.get(url).asString();

        // Parse the page content
        Document doc = Jsoup.parse(page);

        // Get summary
        Element summarySelection = doc.select("#mw-content-text > div.mw-parser-output > p").first();

        String summary = "";
        if (summarySelection != null) {
            summary = summarySelection.html();
        }

        String img = null;
        Element infobox = doc.select("#mw-content-text > div.mw-parser-output > div.notaninfobox > div.infobox-imagearea.animated-container").first();
        if (infobox != null) {
            Element imgEle = infobox.select("img").first();
            if (imgEle != null) {
                img = "https://minecraft.wiki" + imgEle.attr("src");
                // Ensure img is a valid image

                if (!EmbedBuilder.URL_PATTERN.matcher(img).matches()) {
                    img = null;
                }
            }
        }

        // Return the results
        return (new EmbedBuilder()
            .setAuthor("Minecraft Wiki Search Results")
            .setTitle(articleName.replace("_", " "), url)
            .setDescription(FlexmarkHtmlConverter.builder().build().convert(summary)
                .replaceAll("/w/", "https://minecraft.wiki/w/"))
            .setThumbnail(img)
            .build()
        );
    }
}
