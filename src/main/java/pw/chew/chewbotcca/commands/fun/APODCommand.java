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
package pw.chew.chewbotcca.commands.fun;

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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import pw.chew.chewbotcca.util.ResponseHelper;
import pw.chew.chewbotcca.util.RestClient;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;

public class APODCommand extends SlashCommand {

    public APODCommand() {
        this.name = "apod";
        this.help = "Show NASA's Astronomy Picture of the Day for today, or a specified date";
        this.aliases = new String[]{"dailyspace", "astropix", "apix"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "date", "The date in MM/DD/YYYY format, blank for today")
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        try {
            event.replyEmbeds(gatherPicture(ResponseHelper.guaranteeStringOption(event, "date", "astropix"))).queue();
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getChannel().sendTyping().queue();
        try {
            event.reply(gatherPicture(event.getArgs()));
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage());
        }
    }

    private MessageEmbed gatherPicture(String date) {
        if (date.equals("astropix") || date.isBlank()) {
            String[] input = date.split("/");
            if (input.length < 3) {
                throw new IllegalArgumentException("Invalid format! Must be MM/DD/YYYY");
            }
            date = getDateURL(input);
            if (date == null) {
                throw new IllegalArgumentException("Invalid date! Range is June 16th, 1995 to today!");
            }
        }

        String url = String.format("https://apod.nasa.gov/apod/%s.html", date);

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

        // Find the image
        XPath xPath = XPathFactory.newInstance().newXPath();
        String title = "Date: ";
        String img = "https://apod.nasa.gov/apod/";
        try {
            title += ((String) xPath.evaluate("/html/body/center[1]/p[2]/text()", doc, XPathConstants.STRING)).trim();
            Node src = (Node) xPath.evaluate("/html/body/center[1]/p[2]/a/img", doc, XPathConstants.NODE);
            img += src.getAttributes().getNamedItem("src").toString().replace("src=", "").replace("\"", "");
        } catch (NullPointerException ignored) {
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error in XPath Expression!");
        }

        // If 404 somehow
        if (img.equals("https://apod.nasa.gov/apod/")) {
            throw new IllegalArgumentException("Invalid date! Range is June 16th, 1995 to today!");
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setImage(img)
            .setAuthor("NASA Astronomy Picture of the Day")
            .setTitle(title, url);
        return embed.build();
    }

    private String getDateURL(String[] date) {
        // Get current time in CST
        OffsetDateTime current = Instant.now().atOffset(ZoneOffset.of("-06:00"));
        // Parse int from date input
        int month = Integer.parseInt(date[0]);
        int day = Integer.parseInt(date[1]);
        int year = Integer.parseInt(date[2]);
        if (year < 2000)
            year += 2000;

        // No APOD prior to 1995
        if (year < 1995)
            return null;
        // No APOD for the future
        if (year > current.getYear())
            return null;
        if (year == current.getYear()) {
            if (month > current.getMonthValue())
                return null;
            if (month == current.getMonthValue()) {
                if (day > current.getDayOfMonth())
                    return null;
            }
        }
        String yearString = String.join("", Arrays.asList(String.valueOf(year).split("")).subList(2, 4));
        String monthString = String.valueOf(month);
        if (monthString.length() == 1) {
            monthString = "0" + monthString;
        }
        String dayString = String.valueOf(day);
        if (dayString.length() == 1) {
            dayString = "0" + dayString;
        }
        return "ap" + yearString + monthString + dayString;
    }
}
