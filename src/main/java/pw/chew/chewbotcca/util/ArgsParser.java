package pw.chew.chewbotcca.util;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.MiscUtil;

import java.util.ArrayList;
import java.util.List;

public class ArgsParser {
    /**
     * Parse arguments and return a list of members.<br>
     * Supports:<br>
     * 1) User name#0000<br>
     * 2) <@!mention><br>
     * 3) IDs
     *
     * @param arg    the arguments
     * @param amount the amount of members needed to parse
     * @param msg    the message (to get members from)
     * @return a list of (possibly null) members with length amount
     * @throws IllegalArgumentException if only one member can be parsed
     */
    public static List<Member> parseMembers(String arg, int amount, Message msg) {
        // Try convenience method first
        List<Member> mentionedMember = msg.getMentionedMembers();
        if (mentionedMember.size() == amount) {
            return mentionedMember;
        }

        List<Member> members = new ArrayList<>();

        String[] tags = arg.split("#");
        // joe bob#0000 bob joe#0000 becomes "joe bob" "0000 bob joe" "0000", so need amount + 1
        if (tags.length > amount + 1) {
            throw new IllegalArgumentException("Too many tags provided!");
        }

        // First check for args
        String[] args = new String[amount];
        int i = 0;

        String temp = "";
        for (String anArg : arg.split(" ")) {
            if ((i + 1) > args.length) {
                throw new IllegalArgumentException("Too many arguments provided!");
            }
            // Check for mention
            if (Mention.isValidMention(Message.MentionType.USER, anArg)) {
                args[i] = anArg;
                i++;
                continue;
            }

            // Check for User ID
            if (anArg.length() >= 17) {
                try {
                    MiscUtil.parseSnowflake(anArg);
                    args[i] = anArg;
                    i++;
                    continue;
                } catch (NumberFormatException ignored) {}
            }

            // Check if it's a valid tag on its own
            if (isValidTag(anArg)) {
                args[i] = anArg;
                i++;
                temp = ""; // Reset temp just in case
                continue;
            }

            // Check if temp is valid
            if (isValidTag(temp)) {
                args[i] = anArg;
                i++;
                temp = "";
            } else {
                // Add to temp and pray for the best
                temp += anArg;
            }
        }

        // Cycle through each split arg
        for (String anArg : args) {
            if (anArg == null) {
                members.add(null);
                continue;
            }
            try {
                // Try parsing ID
                long id = Long.parseLong(anArg);
                members.add(msg.getGuild().retrieveMemberById(id).complete());
                continue;
            } catch (NumberFormatException e) {
                // ID failed, let's try parsing mention
                Object parsed = Mention.parseMention(anArg, msg.getGuild(), msg.getJDA());
                if (parsed instanceof Member) {
                    members.add((Member) parsed);
                    continue;
                }
                // Okay, that failed. Let's check if it's a tag
                if (parsed == null && isValidTag(anArg)) {
                    members.add(msg.getGuild().getMemberByTag(anArg));
                    continue;
                }
            }
            // If all else fails...
            members.add(null);
        }
        return members;
    }

    /**
     * Checks for "Valid Tag#0000"
     *
     * @param input the input to test
     * @return true if valid, false if not
     */
    private static boolean isValidTag(String input) {
        if (!input.contains("#")) {
            return false;
        }

        String[] test = input.split("#");
        if (test.length > 2) {
            return false;
        }

        // Not valid discriminator, final check so IntelliJ decided to merge these. thanks.
        return test[1].length() == 4;
    }
}
