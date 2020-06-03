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
                .setDescription("Chewbotcca is a multi-purpose, semi-functional, almost always online, discord bot!\n\n" +
                        "This bot is powered by [SkySilk Cloud Services](https://www.skysilk.com/ref/4PRQpuQraD)!")
                .addField("Commands", "You can find all my commands [here](https://chew.pw/chewbotcca/discord/commands)", true)
                .addField("Invite me!", "You can invite me to your server with [this link](https://discord.com/oauth2/authorize?client_id=604362556668248095&scope=bot&permissions=0).", true)
                .addField("Help Server", "Click [me](https://discord.gg/hUvyjeQ) to join the help server.", true)
                .addField("More Bot Stats", "Run `%^stats` to see more stats!", true)
        .build());
    }
}
