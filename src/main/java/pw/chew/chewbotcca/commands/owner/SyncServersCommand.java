package pw.chew.chewbotcca.commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import pw.chew.chewbotcca.listeners.ServerJoinLeaveListener;

public class SyncServersCommand extends Command {

    public SyncServersCommand() {
        this.name = "syncservers";
        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        ServerJoinLeaveListener.syncStats(1, event.getJDA().getGuildCache().size());
    }
}
