package pw.chew.chewbotcca.commands.quotes;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import pro.chew.api.ChewAPI;

public class TRBMBCommand extends Command {

    public TRBMBCommand() {
        this.name = "trbmb";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply(new ChewAPI().getTRBMBPhrase());
    }
}
