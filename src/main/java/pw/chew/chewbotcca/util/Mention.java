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
package pw.chew.chewbotcca.util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public class Mention {
    public static Object parseMention(String mention, JDA jda) {
        if(!mention.contains("<") && !mention.contains(">"))
            return null;

        mention = mention.replace("<", "").replace(">", "");
        if(mention.startsWith("@!")) {
            return jda.getUserById(mention.replace("@!", ""));
        }
        return null;
    }

    public static Object parseMention(String mention, Guild server, JDA jda) {
        if(!mention.contains("<") && !mention.contains(">"))
            return null;

        mention = mention.replace("<", "").replace(">", "");
        if(mention.startsWith("@!")) {
            return jda.getUserById(mention.replace("@!", ""));
        }
        if(mention.startsWith("#")) {
            return server.getGuildChannelById(mention.replace("#", ""));
        }
        if(mention.startsWith("@&")) {
            return server.getRoleById(mention.replace("@&", ""));
        }
        return null;
    }
}
