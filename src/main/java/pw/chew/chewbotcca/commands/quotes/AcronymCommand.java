package pw.chew.chewbotcca.commands.quotes;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import pro.chew.api.ChewAPI;

public class AcronymCommand extends Command {

    public AcronymCommand() {
        this.name = "acronym";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String phrase = new ChewAPI().generateAcronym(event.getArgs());
            event.reply("Acronym for " + event.getArgs() + " is " + phrase);
        } catch (IllegalArgumentException e) {
            event.reply("Args must only contain letters!");
        }
    }
}
