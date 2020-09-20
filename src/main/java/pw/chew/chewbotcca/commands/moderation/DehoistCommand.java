package pw.chew.chewbotcca.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

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

        // Load members
        new Thread(() -> event.getGuild().loadMembers().get());
        await().atMost(30, TimeUnit.SECONDS).until(() -> event.getGuild().getMemberCache().size() == event.getGuild().getMemberCount());

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
