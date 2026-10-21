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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.MiscUtil;

public class Mention {
    public static User parseUserMention(String mention, JDA jda) {
        if(!mention.contains("<") && !mention.contains(">"))
            return null;

        mention = mention.replace("<", "").replace(">", "");
        if(mention.startsWith("@!")) {
            return jda.getUserById(mention.replace("@!", ""));
        }
        throw new IllegalArgumentException("No valid mention string in message!");
    }

    public static Object parseMention(String mention, Guild server, JDA jda) {
        if(!mention.contains("<") && !mention.contains(">"))
            return null;

        mention = mention.replace("<", "").replace(">", "");
        if(mention.startsWith("@!")) {
            String id = mention.replace("@!", "");
            Member member = server.getMemberById(id);
            if (member == null) {
                return jda.retrieveUserById(id).complete();
            } else {
                return member;
            }
        }
        if(mention.startsWith("#")) {
            return server.getGuildChannelById(mention.replace("#", ""));
        }
        if(mention.startsWith("@&")) {
            return server.getRoleById(mention.replace("@&", ""));
        }
        return null;
    }

    public static boolean isValidMention(Message.MentionType type, String mention) {
        String internal = "not numbers";
        if (!(mention.startsWith("<") && mention.endsWith(">"))) {
            return false;
        }
        switch (type) {
            case USER -> {
                if (mention.startsWith("<@!")) {
                    internal = mention.replaceAll("[<>!@]", "");
                } else {
                    return false;
                }
            }
            case CHANNEL -> {
                if (mention.startsWith("<#")) {
                    internal = mention.replaceAll("[<>#]", "");
                } else {
                    return false;
                }
            }
            case ROLE -> {
                if (mention.startsWith("@&")) {
                    internal = mention.replaceAll("[<>@&]", "");
                } else {
                    return false;
                }
            }
        }
        if (internal.length() < 17) {
            return false;
        }
        try {
            MiscUtil.parseSnowflake(internal);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
