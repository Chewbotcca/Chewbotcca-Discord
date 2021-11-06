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
package pw.chew.chewbotcca.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.util.Arrays;
import java.util.List;

public class DehoistCommand extends Command {
    // List from https://github.com/jagrosh/Vortex/blob/e1f0/src/main/java/com/jagrosh/vortex/utils/OtherUtil.java#L37-L40
    public final static List<Character> DEHOIST_ORIGINAL = Arrays.asList('!', '"', '#', '$', '%',
        '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/');
    public final static char[] DEHOIST_REPLACEMENTS = {'\u01C3', '\u201C', '\u2D4C', '\uFF04', '\u2105',     // visually
        '\u214B', '\u2018', '\u2768', '\u2769', '\u2217', '\u2722', '\u201A', '\u2013', '\u2024', '\u2044'}; // similar

    public DehoistCommand() {
        this.name = "dehoist";
        this.guildOnly = true;
        this.userPermissions = new Permission[]{Permission.NICKNAME_MANAGE};
        this.botPermissions = new Permission[]{Permission.NICKNAME_MANAGE};
        this.cooldown = 30;
        this.cooldownScope = CooldownScope.GUILD;
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getChannel().sendTyping().queue();

        int successful = 0;
        for (Member member : event.getGuild().getMembers()) {
            boolean wasSuccessful = dehoist(member);
            if (wasSuccessful)
                successful++;
        }

        event.reply("Successfully dehoisted " + successful + " members! Check Audit Log for more info.");
    }

    /**
     * Attempt to dehoist a given member
     * @param member the member
     */
    private boolean dehoist(Member member) {
        if(!member.getGuild().getSelfMember().canInteract(member))
            return false;

        if (DEHOIST_ORIGINAL.contains(member.getEffectiveName().charAt(0))) {
            int i = DEHOIST_ORIGINAL.indexOf(member.getEffectiveName().charAt(0));
            String nick = member.getEffectiveName().replace(DEHOIST_ORIGINAL.get(i), DEHOIST_REPLACEMENTS[i]);
            member.modifyNickname(nick).reason("Dehoisting").queue();
            return true;
        }

        return false;
    }
}
