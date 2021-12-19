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
}
