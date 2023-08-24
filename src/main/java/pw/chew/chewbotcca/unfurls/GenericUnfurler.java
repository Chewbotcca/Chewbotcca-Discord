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
package pw.chew.chewbotcca.unfurls;

import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.Nullable;

public interface GenericUnfurler {
    /**
     * Checks to see if this link can be handled by this unfurler.
     *
     * @param link the link to check
     * @return true if it can, false if not
     */
    boolean checkLink(String link);

    /**
     * Unfurls a given link. Should be checked with {@link #checkLink} first.
     *
     * @param link the link to unfurl
     * @return the unfurled embed, or null if it couldn't be unfurled
     */
    @Nullable
    MessageEmbed unfurl(String link);
}
