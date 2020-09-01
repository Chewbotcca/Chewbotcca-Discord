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
