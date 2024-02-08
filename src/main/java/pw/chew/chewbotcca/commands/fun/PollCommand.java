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

package pw.chew.chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.Nullable;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import pw.chew.chewbotcca.objects.Memory;
import pw.chew.chewbotcca.objects.PollEmbed;
import pw.chew.chewbotcca.objects.PollVoter;
import pw.chew.chewbotcca.util.MiscUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PollCommand extends SlashCommand {
    private static final DB db = DBMaker.fileDB("polls.db").fileMmapEnable().closeOnJvmShutdown().make();
    private static final HTreeMap<String, PollVoter> pollVoterMap = db
        .hashMap("voters", Serializer.STRING, new PollVoter.EntrySerializer())
        .createOrOpen();

    public PollCommand() {
        this.name = "poll";
        this.help = "Create a poll! Use the options to configure it, then a modal will appear for you to add questions.";
        this.guildOnly = true;
        this.options = Arrays.asList(
            new OptionData(OptionType.INTEGER, "options", "The number of options to choose from", true)
                .setRequiredRange(1, 10)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        int options = event.getOption("options", 3, OptionMapping::getAsInt);

        String id = String.format("poll-%s-%s", event.getChannel().getId(), System.currentTimeMillis());

        var modal = Modal.create(id, "Fill out your poll information");
        modal.addActionRow(
            TextInput.create(id + "-question", "Enter the question", TextInputStyle.SHORT).setRequired(true).build()
        );
        modal.addActionRow(
            TextInput.create(id + "-description", "Enter a more detailed description", TextInputStyle.PARAGRAPH)
                .setRequired(false).build()
        );
        for (int i = 0; i < options; i++) {
            modal.addActionRow(
                TextInput.create(id + "-option-" + i, "Enter option " + (i + 1), TextInputStyle.SHORT).setRequired(true).build()
            );
        }

        event.replyModal(modal.build()).queue();

        Memory.getWaiter().waitForEvent(ModalInteractionEvent.class, e -> e.getModalId().equals(id), e -> {
            var question = e.getValue(id + "-question").getAsString();
            var description = e.getValue(id + "-description").getAsString();
            List<String> choices = new ArrayList<>();
            for (int i = 0; i < options; i++) {
                choices.add(e.getValue(id + "-option-" + i).getAsString());
            }

            String user = String.format("%s (%s)", MiscUtil.getTag(event.getUser()), event.getUser().getId());

            PollEmbed embed = new PollEmbed(id, question, description, choices, user);

            e.replyEmbeds(embed.buildEmbed()).setComponents(embed.buildActionRow()).queue();
        });
    }

    /**
     * Sends the "Information" embed with options to view voters or close the poll.
     *
     * @param pollId The poll ID
     * @param event  The event that triggered the interaction
     */
    public static void sendInfoEmbed(String pollId, ButtonInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("What would you like to do?")
            .setFooter("Poll message: " + event.getMessageId());

        PollEmbed pollEmbed = PollEmbed.fromEmbed(event.getMessage().getEmbeds().get(0), pollId);

        PollVoter voter = getVote(pollId, event.getUser().getId());
        if (voter != null) {
            embed.addField("Your Vote", pollEmbed.getChoice(voter.choice()), false);
        }

        Button closeButton = Button.danger(pollId + "-close", "Close Poll")
            .withDisabled(!pollEmbed.getAuthorId().equals(event.getUser().getId()));

        event.replyEmbeds(embed.build())
            .addActionRow(Button.secondary(pollId + "-voters", "Voters"), closeButton)
            .setEphemeral(true).queue();
    }

    /**
     * Closes the poll, but only the poll author can do this.
     *
     * @param pollId The poll ID
     * @param event  The event that triggered the interaction
     */
    public static void closePoll(String pollId, ButtonInteractionEvent event) {
        String msgId = event.getMessage().getEmbeds().get(0).getFooter().getText().split(": ")[1];
        event.getChannel().retrieveMessageById(msgId).queue(message -> {
            PollEmbed embed = PollEmbed.fromEmbed(message.getEmbeds().get(0), pollId);

            message.editMessageComponents(embed.buildActionRow(true))
                .queue(unused -> event.reply("Poll closed!").setEphemeral(true).queue());
        });
    }

    /**
     * Shows the initial voters embed.
     *
     * @param pollId The poll ID
     * @param event  The event that triggered the interaction
     */
    public static void showVoters(String pollId, ButtonInteractionEvent event) {
        String msgId = event.getMessage().getEmbeds().get(0).getFooter().getText().split(": ")[1];
        event.getChannel().retrieveMessageById(msgId).queue(message -> {
            PollEmbed embed = PollEmbed.fromEmbed(message.getEmbeds().get(0), pollId);

            StringSelectMenu menu = buildChoicesMenu(embed.getChoices(), pollId, 0);

            event.replyEmbeds(getVotersEmbed(getVoters(pollId, 0), embed.getChoice(0)))
                .addActionRow(menu)
                .setEphemeral(true)
                .queue();
        });
    }

    /**
     * Switches the voter page based on the selected option.
     *
     * @param pollId The poll ID
     * @param event  The event that triggered the interaction
     */
    public static void switchVotersPage(String pollId, StringSelectInteractionEvent event) {
        var selected = event.getSelectedOptions().get(0);
        String choice = selected.getEmoji().getFormatted() + " " + selected.getLabel();

        // Get the position of the selected option in the menu
        int position = event.getSelectMenu().getOptions().indexOf(selected);
        Collection<PollVoter> voters = getVoters(pollId, position);

        List<String> choices = event.getSelectMenu().getOptions().stream()
            .map(option -> option.getEmoji().getFormatted() + " " + option.getLabel()).toList();

        MessageEmbed embed = getVotersEmbed(voters, choice);

        event.editMessageEmbeds(embed).setActionRow(buildChoicesMenu(choices, pollId, position)).queue();
    }

    /**
     * Generates the voters embed, which shows the voters for a specific choice.
     *
     * @param voters The voters for this choice
     * @param choice The choice name
     * @return The voters embed
     */
    private static MessageEmbed getVotersEmbed(Collection<PollVoter> voters, String choice) {
        EmbedBuilder embed = new EmbedBuilder().setTitle("Voters");

        List<String> voteMentions = voters.stream().map(v -> String.format("<@%s>", v.userId())).toList();

        return embed.setDescription(choice + "\n\n" + String.join(", ", voteMentions)).build();
    }

    /**
     * Builds the choices select menu for the voters embed.
     *
     * @param choices  The choices
     * @param pollId   The poll ID
     * @param selected The selected option
     * @return The select menu
     */
    private static StringSelectMenu buildChoicesMenu(List<String> choices, String pollId, int selected) {
        var menu = StringSelectMenu.create(pollId + "-voters-menu")
            .setPlaceholder("Select a option to see voters")
            .setRequiredRange(1, 1);

        var results = getResults(pollId, choices.size());

        for (int i = 0; i < choices.size(); i++) {
            // Label, Value, Description, Emoji
            // Choice Name, Choice Number, Number of votes, Emoji
            String choice = choices.get(i);
            String emoji = choice.split(" ")[0];
            String name = choice.split("> ")[1];

            menu.addOption(
                name,
                String.valueOf(i),
                String.format("%s votes", results.get(i)),
                Emoji.fromFormatted(emoji)
            );
        }

        menu.setDefaultOptions(menu.getOptions().get(selected));

        return menu.build();
    }

    /**
     * Adds a vote to the database
     *
     * @param pollId The poll ID
     * @param voter  The voter
     */
    public static void addVote(String pollId, PollVoter voter) {
        pollVoterMap.put(pollId+"-"+voter.userId(), voter);
        db.commit();
    }

    /**
     * Gets all voters for a given poll.
     *
     * @param pollId The poll ID
     * @return The voters
     */
    public static Collection<PollVoter> getVoters(String pollId) {
        return pollVoterMap.values().stream().filter(voter -> voter.pollId().equals(pollId)).toList();
    }

    /**
     * Gets all voters for a specific poll, but sort by choice
     *
     * @param pollId The poll ID
     * @param choice The choice
     * @return The voters
     */
    public static Collection<PollVoter> getVoters(String pollId, int choice) {
        return getVoters(pollId).stream().filter(voter -> voter.choice() == choice).toList();
    }

    /**
     * Gets a vote for a specific voter
     *
     * @param pollId The poll ID
     * @param userId The user ID
     * @return The vote
     */
    @Nullable
    public static PollVoter getVote(String pollId, String userId) {
        return pollVoterMap.get(pollId+"-"+userId);
    }

    /**
     * Gets a map of poll choices to the amount of voters
     *
     * @param pollId  The poll ID
     * @param choices The amount of choices
     * @return The results
     */
    public static Map<Integer, Integer> getResults(String pollId, int choices) {
        Collection<PollVoter> voters = PollCommand.getVoters(pollId);
        Map<Integer, Integer> results = new HashMap<>();
        for (int i = 0; i < choices; i++) {
            results.put(i, 0);
        }
        for (PollVoter voter : voters) {
            results.put(voter.choice(), results.getOrDefault(voter.choice(), 0) + 1);
        }
        return results;
    }
}
