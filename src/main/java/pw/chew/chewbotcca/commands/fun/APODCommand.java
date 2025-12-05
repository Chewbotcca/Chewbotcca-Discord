/*
 * Copyright (C) 2025 Chewbotcca
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

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import pw.chew.chewbotcca.util.CommandContext;
import pw.chew.chewbotcca.util.RestClient;

import javax.annotation.Nullable;
import java.text.DateFormatSymbols;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h2><code>/apod</code> Command</h2>
 *
 * <a href="https://help.chew.pro/bots/discord/chewbotcca/commands/apod">Docs</a>
 */
public class APODCommand extends SlashCommand {
    private static final Map<String, Astropix> cache = new HashMap<>();

    public APODCommand() {
        this.name = "apod";
        this.help = "Show NASA's Astronomy Picture of the Day for today, or a specified date";
        this.contexts = CommandContext.GLOBAL;
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
            long month = event.optLong("month", today.getMonthValue());
            long day = event.optLong("day", today.getDayOfMonth());
            long year = event.optLong("year", today.getYear());
            String date = getDateURL(year, month, day);

            Astropix pic = cache.get(date);
            if (pic == null) {
                pic = retrievePicture(date);
                cache.put(date, pic);
            }

            event.replyComponents(pic.buildContainer())
                .useComponentsV2()
                .queue();
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    private static Astropix retrievePicture(String date) {
        return RestClient.get("https://api.chew.pro/apod?date=%s".formatted(date)).asGsonObject(Astropix.class);
    }

    private static String getDateURL(long year, long month, long day) {
        // Get current time in CST
        OffsetDateTime current = OffsetDateTime.now();
        // Parse int from date input
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
        return yearString + monthString + dayString;
    }

    public static void replyExplanation(ButtonInteractionEvent event, String date) {
        Astropix pic = cache.get(date);
        if (pic == null) {
            pic = retrievePicture(date);
            cache.put(date, pic);
        }

        event.reply(pic.explanation()).setEphemeral(true).queue();
    }

    private List<Command.Choice> buildMonthChoices() {
        List<Command.Choice> responses = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String month = new DateFormatSymbols().getMonths()[i - 1];
            responses.add(new Command.Choice(month, i));
        }
        return responses;
    }

    private record Astropix(String friendlyDate, String title, @Nullable String description, String img, String url, String explanation) {
        private final static String NO_PICTURE = "Could not find an image for this date! You will need to visit the page to enjoy today's \"picture\".";

        public String description() {
            String desc = description;
            if (desc == null) {
                return "No description provided. See explanation for more details.";
            }
            desc = desc.replaceAll("Clicking on the picture will download the highest resolution version available.", "");
            return desc;
        }

        private Container buildContainer() {
            return Container.of(
                TextDisplay.of("## NASA Astronomy Picture of the Day"),
                TextDisplay.of("### [%s](%s)".formatted(title, url)),
                TextDisplay.of(description()),
                TextDisplay.of("-# " + friendlyDate),
                Separator.createDivider(Separator.Spacing.SMALL),
                img == null ? TextDisplay.of(NO_PICTURE) : MediaGallery.of(
                    MediaGalleryItem.fromUrl(img)
                ),
                Separator.createDivider(Separator.Spacing.SMALL),
                ActionRow.of(Button.primary(
                    "apod:explanation:%s".formatted(url.substring(29, 35)),
                    "View Explanation"
                ), Button.link(url, "View Online"))
            );
        }
    }
}
