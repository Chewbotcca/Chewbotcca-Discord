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
package pw.chew.chewbotcca.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.Color;

/**
 * This class provides some methods to help with responding to commands.
 */
public class ResponseHelper {
    /**
     * Generates a "Failure" embed.
     *
     * @param title       a title to override
     * @param description a description to override
     * @return the failure embed
     */
    public static MessageEmbed generateFailureEmbed(String title, String description) {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("Uh oh, something went wrong!")
            .setDescription("Please try the command again")
            .setColor(Color.RED);

        if (title != null)
            embed.setTitle(title);

        if (description != null)
            embed.setDescription(description);

        return embed.build();
    }

    /**
     * Guarantees a String option value. Removes the "can be null" check because we know it isn't.
     *
     * @param event the slash command event to get options from
     * @param option the option we want
     * @param fallback if the option doesn't exist, what should we use instead?
     * @return the never-null option
     */
    public static String guaranteeStringOption(SlashCommandEvent event, String option, String fallback) {
        for (OptionMapping optionMapping : event.getOptions()) {
            if (optionMapping.getName().equals(option)) {
                return optionMapping.getAsString();
            }
        }
        return fallback;
    }

    /**
     * Guarantees a boolean option value. Removes the "can be null" check because we know it isn't.
     *
     * @param event the slash command event to get options from
     * @param option the option we want
     * @param fallback if the option doesn't exist, what should we use instead?
     * @return the never-null option
     */
    public static boolean guaranteeBooleanOption(SlashCommandEvent event, String option, boolean fallback) {
        for (OptionMapping optionMapping : event.getOptions()) {
            if (optionMapping.getName().equals(option)) {
                return optionMapping.getAsBoolean();
            }
        }
        return fallback;
    }
}
