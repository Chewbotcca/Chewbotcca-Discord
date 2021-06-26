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
package pw.chew.chewbotcca.commands.minecraft;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import pw.chew.chewbotcca.util.ResponseHelper;
import pw.chew.chewbotcca.util.RestClient;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// %^mcwiki command
public class MCWikiCommand extends SlashCommand {

    public MCWikiCommand() {
        this.name = "mcwiki";
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
            event.replyEmbeds(gatherData(ResponseHelper.guaranteeStringOption(event, "query", ""))).queue();
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        try {
            commandEvent.reply(gatherData(commandEvent.getArgs().strip()));
        } catch (IllegalArgumentException e) {
            commandEvent.replyWarning(e.getMessage());
        }
    }

    private MessageEmbed gatherData(String query) {
        String apiUrl = "https://minecraft.gamepedia.com/api.php?action=opensearch&search=";
        String mcUrl = "https://minecraft.gamepedia.com/";

        JSONArray j;

        // Try and find a result
        try {
            j = new JSONArray(RestClient.get(apiUrl + URLEncoder.encode(query, StandardCharsets.UTF_8))).getJSONArray(1);
        } catch (JSONException e) {
            e.printStackTrace();
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
            String name = str.replace("https://minecraft.gamepedia.com/", "");
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
        String page = RestClient.get(url);

        // Configure parser for later
        TagNode tagNode = new HtmlCleaner().clean(page);
        Document doc;
        try {
            doc = new DomSerializer(new CleanerProperties()).createDOM(tagNode);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error configuring Parser!");
        }

        // Find the summary text and image (if there is one)
        XPath xPath = XPathFactory.newInstance().newXPath();
        String summary = null;
        String img = null;
        try {
            summary = (String) xPath.evaluate("//*[@id=\"mw-content-text\"]/div/p[1]", doc, XPathConstants.STRING);
            Node src = (Node) xPath.evaluate("//*[@id=\"mw-content-text\"]/div/div[1]/div[2]/div[1]/a/img", doc, XPathConstants.NODE);
            img = src.getAttributes().getNamedItem("src").toString().replace("src=", "").replace("\"", "");
        } catch (NullPointerException ignored) {
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error in XPath Expression!");
        }

        // Return the results
        return (new EmbedBuilder()
            .setAuthor("Minecraft Wiki Search Results")
            .setTitle(articleName.replace("_", " "), url)
            .setDescription(summary)
            .setThumbnail(img)
            .build()
        );
    }
}
