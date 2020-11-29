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

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
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
import java.util.Date;

public class APODCommand extends Command {

    public APODCommand() {
        this.name = "apod";
        this.aliases = new String[]{"dailyspace", "astropix", "apix"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        String date = "astropix";
        if (!event.getArgs().isBlank()) {
            String[] input = event.getArgs().split("/");
            if (input.length < 3) {
                event.reply("Invalid format! Must be MM/DD/YYYY");
                return;
            }
            date = getDateURL(input);
            if (date == null) {
                event.reply("Invalid date! Range is June 16th, 1995 to today!");
                return;
            }
        }

        event.getChannel().sendTyping().queue();

        String url = String.format("https://apod.nasa.gov/apod/%s.html", date);

        String page = RestClient.get(url);

        // Configure parser for later
        TagNode tagNode = new HtmlCleaner().clean(page);
        Document doc;
        try {
            doc = new DomSerializer(new CleanerProperties()).createDOM(tagNode);
        } catch (ParserConfigurationException e) {
            event.reply("Error configuring Parser!");
            e.printStackTrace();
            return;
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
            event.reply("Error in XPath Expression!");
            e.printStackTrace();
            return;
        }

        // If 404 somehow
        if (img.equals("https://apod.nasa.gov/apod/")) {
            event.reply("Invalid date! Range is June 16th, 1995 to today!");
            return;
        }

        MessageChannel channel = event.getChannel();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setImage(img)
            .setAuthor("NASA Astronomy Picture of the Day")
            .setTitle(title, url);
        channel.sendMessage(embed.build()).queue();
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

        Date parse = new Date(year - 1900, month, day);

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
