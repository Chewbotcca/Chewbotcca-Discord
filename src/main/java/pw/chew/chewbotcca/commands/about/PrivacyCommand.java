package pw.chew.chewbotcca.commands.about;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class PrivacyCommand extends Command {

    public PrivacyCommand() {
        this.name = "privacy";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply("You can view the Chewbotcca privacy policy here: https://chew.pw/chewbotcca/discord/privacy");
    }
}
