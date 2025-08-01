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

package pw.chew.chewbotcca.objects;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.internal.utils.Checks;
import pw.chew.chewbotcca.commands.fun.PollCommand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Wraps around a poll embed to handle stuff relating to the poll.
 *
 * @param pollId The ID of the poll
 * @param title The title of the poll
 * @param description The description of the poll
 * @param choices The choices of the poll
 * @param author The author of the poll
 */
public record PollEmbed(String pollId, String title, String description, List<String> choices, String author) {
    // emoji regional_indicators for each letter
    private static final String[] EMOJI = new String[]{
        "<:letter_a:1034691614792241282>",
        "<:letter_b:1034691618311241748>",
        "<:letter_c:1034691615719182366>",
        "<:letter_d:1034691616545443880>",
        "<:letter_e:1034691611571015740>",
        "<:letter_f:1034691612166598677>",
        "<:letter_g:1034691617459802193>",
        "<:letter_h:1034691613231943790>",
        "<:letter_i:1034691610727944212>",
        "<:letter_j:1034691614007885904>"
    };

    /**
     * Generates a {@link PollEmbed} from a {@link MessageEmbed}.
     *
     * @param embed The embed to generate from
     * @param pollId The ID of the poll
     * @return The generated poll embed
     */
    public static PollEmbed fromEmbed(MessageEmbed embed, String pollId) {
        String title = embed.getTitle();
        String description = embed.getDescription();

        String value = embed.getFields().getFirst().getValue();
        Checks.notNull(value, "Poll choices cannot be null!");
        String[] rows = value.split("\n");
        // Look for rows that start with an emoji from the array
        // If it does, add it to the list
        List<String> choices = new ArrayList<>();
        for (String row : rows) {
            if (row.startsWith("<:")) {
                choices.add(row.substring(32));
            }
        }

        String author = embed.getFooter().getText().split("Started by ")[1];

        return new PollEmbed(pollId, title, description, choices, author);
    }

    /**
     * Builds the poll embed for use in Discord.
     *
     * @return The built embed
     */
    public MessageEmbed buildEmbed() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < choices.size(); i++) {
            builder.append(EMOJI[i]).append(" ").append(choices.get(i))
                .append("\n")
                // Now for the progress bar
                .append(buildProgressBar(i))
                .append("\n");
        }

        return new EmbedBuilder()
            .setTitle(title)
            .setDescription(description)
            .addField("Choices", builder.toString(), false)
            .setFooter("You may select only one option! | ⚙️ Settings/Info\nStarted by " + author)
            .build();
    }

    /**
     * Builds the action row of choices to select.
     *
     * @param closed Whether the poll is closed or not
     * @return The built action row
     */
    public ActionRow buildActionRow(boolean closed) {
        List<Button> buttons = new ArrayList<>();
        for (int i = 0; i < choices.size(); i++) {
            Button button = Button.secondary(pollId + "-choice-" + i, String.valueOf(getResults().get(i)))
                .withEmoji(Emoji.fromFormatted(EMOJI[i]));
            if (closed) {
                button = button.asDisabled();
            }
            buttons.add(button);
        }

        Button settings = Button.secondary(pollId + "-settings", Emoji.fromUnicode("⚙️"));
        if (closed) {
            settings = settings.asDisabled();
        }
        buttons.add(settings);

        return ActionRow.of(buttons);
    }

    /**
     * Builds the action row of choices to select.
     *
     * @return The built action row
     */
    public ActionRow buildActionRow() {
        return buildActionRow(false);
    }

    /**
     * Builds a progress bar for a given choice.
     *
     * @param choice The choice to build the progress bar for
     * @return The progress bar
     */
    private String buildProgressBar(int choice) {
        Collection<PollVoter> voters = PollCommand.getVoters(pollId);

        int total = voters.size();
        int count = 0;
        for (PollVoter voter : voters) {
            if (voter.choice() == choice) {
                count++;
            }
        }

        double percent = (double) count / total;
        String friendlyPercent = String.format("%.2f", percent * 100);
        if (friendlyPercent.endsWith(".00")) {
            friendlyPercent = friendlyPercent.substring(0, friendlyPercent.length() - 3);
        }
        int barLength = (int) (percent * 20);

        return "`" +
            "█".repeat(Math.max(0, barLength)) +
            " ".repeat(Math.max(0, 20 - barLength)) +
            "`" + " | " +
            friendlyPercent +
            "% (" + count + ")";
    }

    /**
     * Gets the results for this poll.
     *
     * @return The results
     */
    public Map<Integer, Integer> getResults() {
        return PollCommand.getResults(pollId, choices.size());
    }

    /**
     * Builds a choice string for a given choice
     *
     * @param choice The choice to build the string for
     * @return The choice string
     */
    public String getChoice(int choice) {
        return EMOJI[choice] + " " + choices.get(choice);
    }

    /**
     * Gets all choices for this poll, formatted
     *
     * @return The choices
     */
    public List<String> getChoices() {
        // Formatted with getChoice
        List<String> formatted = new ArrayList<>();
        for (int i = 0; i < choices.size(); i++) {
            formatted.add(getChoice(i));
        }
        return formatted;
    }

    /**
     * The author ID of the poll.
     *
     * @return The author ID
     */
    public String getAuthorId() {
        // Author String: Bruh#1234 (53435435435) <-- we need the number
        return author.substring(author.indexOf("(") + 1, author.indexOf(")"));
    }
}
