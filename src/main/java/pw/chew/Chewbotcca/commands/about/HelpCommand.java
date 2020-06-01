package pw.chew.Chewbotcca.commands.about;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

public class HelpCommand extends Command {
    public HelpCommand() {
        this.name = "help";
        this.help = "Get Help with the bot";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.reply(new EmbedBuilder()
                .setTitle("Welcome to the Chewbotcca Discord Bot")
                .setColor(0xd084)
                .setDescription("Chewbotcca is a multi-purpose, semi-functional, almost always online, discord bot!")
                .addField("Commands", "You can find all my commands [here](http://discord.chewbotcca.co/commands)", true)
                .addField("Invite me!", "You can invite me to your server with [this link](http://bit.ly/Chewbotcca).", true)
                .addField("Help Server", "Click [me](https://discord.gg/Q8TazNz) to join the help server.", true)
                .addField("More Bot Stats", "Run `%^stats` to see more stats!", true)
        .build());
    }
}
