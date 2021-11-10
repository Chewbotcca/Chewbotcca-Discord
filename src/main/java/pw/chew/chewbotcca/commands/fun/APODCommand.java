/*
 * Copyright (C) 2021 Chewbotcca
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
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import pw.chew.chewbotcca.util.ResponseHelper;
import pw.chew.chewbotcca.util.RestClient;

import java.text.DateFormatSymbols;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class APODCommand extends SlashCommand {

    public APODCommand() {
        this.name = "apod";
        this.help = "Show NASA's Astronomy Picture of the Day for today, or a specified date";
        this.aliases = new String[]{"dailyspace", "astropix", "apix"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.options = List.of(
            new OptionData(OptionType.INTEGER, "year", "The year for the pic, blank for this year. Range: (1995-)")
                .setMinValue(1995).setMaxValue(OffsetDateTime.now().getYear()),
            new OptionData(OptionType.INTEGER, "month", "The month for the pic, blank for this month.")
                .addChoices(buildMonthChoices()),
            new OptionData(OptionType.INTEGER, "day", "The day for the pic, blank for this day.")
                .setMinValue(1).setMaxValue(31)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        try {
            // Check for no input
            OffsetDateTime today = OffsetDateTime.now();
            String month = ResponseHelper.guaranteeStringOption(event, "month", String.valueOf(today.getMonthValue()));
            String day = ResponseHelper.guaranteeStringOption(event, "day", String.valueOf(today.getDayOfMonth()));
            String year = ResponseHelper.guaranteeStringOption(event, "year", String.valueOf(today.getYear()));
            String date = month + "/" + day + "/" + year;

            event.replyEmbeds(gatherPicture(date)).queue();
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
        if (!date.equals("astropix") || date.isBlank()) {
            String[] input = date.split("/");
            if (input.length < 3) {
                throw new IllegalArgumentException("Invalid format! Must be MM/DD/YYYY");
            }
            try {
                date = getDateURL(input);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Error occurred: " + e.getMessage() + " Range is June 16th, 1995 to today!");
            }
        }

        String url = String.format("https://apod.nasa.gov/apod/%s.html", date);

        String page = RestClient.get(url);

        // Parse the page content
        Document doc = Jsoup.parse(page);

        // Get title and img
        String friendlyDate = "Date: " + doc.select("body > center:nth-child(1) > p:nth-child(3)").text();
        String title = doc.select("body > center:nth-child(2) > b:nth-child(1)").text();
        Elements image = doc.select("body > center:nth-child(1) > p:nth-child(3) > a > img");
        String description = image.attr("alt").replaceAll("\n", " ");
        String img = "https://apod.nasa.gov/apod/" + image.attr("src");
        // Ensure img is a valid image
        if (!EmbedBuilder.URL_PATTERN.matcher(img).matches() || img.equals("https://apod.nasa.gov/apod/")) {
            // debug output the image url for debugging
            description = "Could not find an image for this date! You will need to visit the page to enjoy today's \"picture\".";
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setImage(img)
            .setAuthor("NASA Astronomy Picture of the Day")
            .setTitle(title, url)
            .setDescription(description)
            .setFooter(friendlyDate);
        return embed.build();
    }

    private String getDateURL(String[] date) {
        // Get current time in CST
        OffsetDateTime current = OffsetDateTime.now();
        // Parse int from date input
        int month = Integer.parseInt(date[0]);
        int day = Integer.parseInt(date[1]);
        int year = Integer.parseInt(date[2]);

        // Sanity check the year, ensure it's 4 digits
        if (year < 100) {
            if (year >= 95) {
                year += 1900;
            } else {
                year += 2000;
            }
        }

        // No APOD prior to 1995
        if (year < 1995)
            throw new IllegalArgumentException("Year must not be prior to 1995.");
        // No APOD for the future
        if (year > current.getYear())
            throw new IllegalArgumentException("Year must not be later than current year.");
        if (year == current.getYear()) {
            if (month > current.getMonthValue())
                throw new IllegalArgumentException("Month must not be later than current month.");
            if (month == current.getMonthValue()) {
                if (day > current.getDayOfMonth())
                    throw new IllegalArgumentException("Day must not be later than current day.");
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

    private List<Command.Choice> buildMonthChoices() {
        List<Command.Choice> responses = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String month = new DateFormatSymbols().getMonths()[i - 1];
            responses.add(new Command.Choice(month, i));
        }
        return responses;
    }
}
