/*
 * Copyright (C) 2023 Chewbotcca
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
package pw.chew.chewbotcca.listeners;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import pw.chew.chewbotcca.commands.fun.PollCommand;
import pw.chew.chewbotcca.commands.info.InfoCommand;
import pw.chew.chewbotcca.commands.minecraft.WynncraftCommand;
import pw.chew.chewbotcca.objects.PollEmbed;
import pw.chew.chewbotcca.objects.PollVoter;
import pw.chew.chewbotcca.util.MiscUtil;

/**
 * Class to listen to and delegate interaction (button or selection menu) responses
 */
public class InteractionHandler extends ListenerAdapter {
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        // Handle button clicks here
        String id = event.getComponentId();

        if (id.startsWith("poll-") && id.contains("-choice-")) {
            // Full ID is formatted as: poll-%s-%s-choice-%s
            // Poll is is everything up to -choice, Choice is everything after choice-
            String[] split = id.split("-choice-");
            String pollId = split[0];
            int choice = MiscUtil.asInt(split[1]);

            PollVoter voter = new PollVoter(pollId, event.getUser().getId(), choice);
            PollCommand.addVote(pollId, voter);
            PollEmbed embed = PollEmbed.fromEmbed(event.getMessage().getEmbeds().get(0), pollId);
            event.editMessageEmbeds(embed.buildEmbed()).setComponents(embed.buildActionRow()).queue();
        } else if (id.startsWith("poll-") && id.endsWith("-settings")) {
            // Full ID is formatted as: poll-%s-config
            PollCommand.sendInfoEmbed(id.split("-settings")[0], event);
        } else if (id.startsWith("poll-") && id.endsWith("-close")) {
            PollCommand.closePoll(id.split("-close")[0], event);
        } else if (id.startsWith("poll-") && id.endsWith("-voters")) {
            PollCommand.showVoters(id.split("-voters")[0], event);
        } else if (id.startsWith("wynn:")) { // Wynncraft Command
            // Parts. 0 = wynn, 1 = chars/main/refresh, 2 = char name, 3 = char id
            String[] parts = id.split(":");

            if (!event.getUser().equals(event.getMessage().getInteraction().getUser())) {
                event.reply("You cannot use this button!").setEphemeral(true).queue();
                return;
            }

            switch (parts[1]) {
                case "chars" -> WynncraftCommand.handleCharactersButton(event, parts[2]);
                case "main" -> WynncraftCommand.handleMainPlayerPageButton(event, parts[2]);
                case "refresh" -> WynncraftCommand.clearCache(parts[2], parts[3], event);
                case "char" -> {
                    switch (parts[4]) {
                        case "main" -> WynncraftCommand.handleGeneralStats(event, parts[2], parts[3]);
                        case "prof" -> WynncraftCommand.handleProfessionStats(event, parts[2], parts[3]);
                        case "dung" -> WynncraftCommand.handleDungeonStats(event, parts[2], parts[3]);
                        case "quests" -> WynncraftCommand.handleQuestStats(event, parts[2], parts[3]);
                    }
                }
                case "guild" -> {
                    switch (parts[3]) {
                        case "members" -> WynncraftCommand.handleGuildMembers(event, parts[2]);
                    }
                }
            }
        }
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        // Handle selection menu choices here
        switch (event.getComponentId()) {
            case "info:didyoumean" -> InfoCommand.updateInfo(event);
        }

        if (event.getComponentId().startsWith("poll-") && event.getComponentId().endsWith("-voters-menu")) {
            // Full ID is formatted as: poll-%s-config
            PollCommand.switchVotersPage(event.getComponentId().split("-voters-menu")[0], event);
        } else if (event.getComponentId().startsWith("wynn:char:")) {
            WynncraftCommand.handleCharacterSelection(event, event.getComponentId().split("wynn:char:")[1]);
        }
    }
}
