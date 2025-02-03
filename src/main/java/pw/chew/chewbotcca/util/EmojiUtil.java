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

package pw.chew.chewbotcca.util;

import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji;

import java.util.List;

public class EmojiUtil {
    private static List<ApplicationEmoji> emoji;

    public static void initialize(List<ApplicationEmoji> emoji) {
        EmojiUtil.emoji = emoji;
    }

    public enum Emoji {
        YTUP,
        YTDOWN,
        VERIFIED,
        PARTNERED,
        NEWS,
        CHANNEL_NSFW,
        ;

        public ApplicationEmoji get() {
            for (ApplicationEmoji e : emoji) {
                if (e.getName().equals(this.name().toLowerCase())) {
                    return e;
                }
            }

            return null;
        }

        public String mention() {
            return get().getAsMention();
        }
    }
}
