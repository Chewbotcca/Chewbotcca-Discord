package pw.chew.Chewbotcca.commands.about;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

public class InviteCommand extends Command {
    public InviteCommand() {
        this.name = "invite";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.reply("Hello! Invite me to your server here: <http://bit.ly/Chewbotcca>. Join my help server here: https://discord.gg/hUvyjeQ");
    }
}
