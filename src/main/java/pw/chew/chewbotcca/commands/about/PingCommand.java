package pw.chew.chewbotcca.commands.about;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

public class PingCommand extends Command {

    public PingCommand() {
        this.name = "ping";
        this.help = "Ping the bot";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        long time = commandEvent.getMessage().getTimeCreated().toInstant().toEpochMilli();
        commandEvent.getChannel().sendMessage(new EmbedBuilder().setDescription("Checking ping..").build()).queue((msg) -> {
            EmbedBuilder eb = new EmbedBuilder().setDescription("Ping is " + (msg.getTimeCreated().toInstant().toEpochMilli() - time) + "ms");
            msg.editMessage(eb.build()).queue();
        });
        commandEvent.getChannel().getLatestMessageId();
    }
}