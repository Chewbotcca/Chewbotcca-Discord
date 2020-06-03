package pw.chew.Chewbotcca.commands.about;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

public class StatsCommand extends Command {

    public StatsCommand() {
        this.name = "stats";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.reply(new EmbedBuilder()
                .setTitle("Chewbotcca - A basic, yet functioning, discord bot")
                .addField("Author", "<@!476488167042580481>", true)
                .addField("Code", "[View code on GitHub](http://github.com/Chewbotcca/Discord)", true)
                .addField("Library", "JDA 4.1.1_156", true)
                .addField("Server Count", String.valueOf(commandEvent.getJDA().getGuildCache().size()), true)
                .setColor(0xd084)
                .build()
        );
    }
}
